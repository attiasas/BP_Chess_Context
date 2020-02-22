importPackage(Packages.schema);
importPackage(Packages.chess);

var autoMovesBlack = true;
var autoMovesWhite = true;

//<editor-fold desc="EventSet">

var donePopulationEvent = bp.EventSet("Start Event", function (e) {
    return e.name.equals("Done Populate");
});

//<editor-fold desc="Moves">
var moves = bp.EventSet("Moves", function (e) {
    return e instanceof Move;
});

var whiteMoves = bp.EventSet("White Moves",function (e) {
    return moves.contains(e) && (e.source.piece !== null) && (Piece.Color.White.equals(e.source.piece.color));
});

var blackMoves = bp.EventSet("black Moves",function (e) {
    return moves.contains(e) && (e.source.piece !== null) && (Piece.Color.Black.equals(e.source.piece.color));
});

var outBoundsMoves = bp.EventSet("",function (e) {
    return moves.contains(e) && (e.source.row < 0 || e.source.row > 7 || e.source.column < 0 || e.source.column > 7 || e.target.row < 0 || e.target.row > 7 || e.target.column < 0 || e.target.column > 7);
});

var staticMoves = bp.EventSet("Non Moves",function (e) {
    return moves.contains(e) && (e.source.equals(e.target) || e.source.piece === null);
});
//</editor-fold>

//<editor-fold desc="Update DB">
var allExceptStateUpdate = bp.EventSet("", function (e) {
    return moves.contains(e) && !stateUpdate.contains(e);
});

var stateUpdate = bp.EventSet("",function (e) {
    return e.name.equals("StateUpdate");
});
//</editor-fold>

//</editor-fold>

//<editor-fold desc="Helper Functions">
function inRange(row,column)
{
    return row >= 0 && row < 8 && column >= 0 && column < 8;
}

function allMovesFromSourceExcept(source, exceptGroup)
{
    var options = [];

    for(var row = 0; row < 8; row++)
    {
        for(var col = 0; col < 8; col++)
        {
            if(row != source.row || col != source.col)
            {
                var move = Move(source,getCell(row,col));
                var found = false;

                for(var i = 0; i < exceptGroup.length && !found; i++)
                {
                    if(move.equals(exceptGroup[i])) found = true;
                }
                bp.log.info(move + " | " + found);
                if(!found) options.push(move);
            }
        }
    }

    return options;
}
//</editor-fold>

//<editor-fold desc="General Rules">

// Rule : Turn Base Game, White Starts
bp.registerBThread("EnforceTurns",function ()
{
    while (true)
    {
        bp.sync({waitFor:whiteMoves,block:blackMoves});
        bp.sync({waitFor:stateUpdate, block:allExceptStateUpdate});

        bp.sync({waitFor:blackMoves,block:whiteMoves});
        bp.sync({waitFor:stateUpdate, block:allExceptStateUpdate});
    }
});

bp.registerBThread("UpdateDBAfterMove", function ()
{
    while (true)
    {
        var move = bp.sync({waitFor:moves});

        bp.sync({request: CTX.UpdateEvent("UpdateCell",{cell:move.target, piece: move.source.piece}) });
        bp.sync({request: CTX.UpdateEvent("UpdateCell",{cell:move.source, piece: null}) });

        bp.sync({request:bp.Event("StateUpdate",move)});
    }
});


// Rule : Moving Pieces only inside the board bounds.
bp.registerBThread("Movement in bounds",function ()
{
    bp.sync({block:outBoundsMoves});
});
// Rule : Move is allowed only if source has piece on it.
bp.registerBThread("Enforce Movement to a new cell", function ()
{
    bp.sync({block:staticMoves});
});
//</editor-fold>

//<editor-fold desc="Pawn Rules">
CTX.subscribe("Pawn Movement Restriction Rule", "PawnCell", function (pawnCell)
{
    while (true)
    {
        bp.sync({block:allMovesFromSourceExcept(pawnCell,pawnMoves(pawnCell)), waitFor:stateUpdate});
    }
});

function pawnMoves(pawnCell)
{
    var optionalMoves = [];
    var myForward = pawnCell.piece.color.equals(Piece.Color.Black) ? -1 : 1;

    // Rule: "A pawn can capture an enemy piece on either of the two squares diagonally in front of the pawn (but cannot move to those squares if they are vacant)."
    if(inRange(pawnCell.row + myForward,pawnCell.col + 1) && getCell(pawnCell.row + myForward,pawnCell.col + 1).piece !== null) optionalMoves.push(Move(pawnCell, getCell(pawnCell.row + myForward, pawnCell.col + 1)));
    if(inRange(pawnCell.row + myForward,pawnCell.col - 1) && getCell(pawnCell.row + myForward,pawnCell.col - 1).piece !== null) optionalMoves.push(Move(pawnCell, getCell(pawnCell.row + myForward, pawnCell.col - 1)));
    // Rule: "A pawn moves straight forward one square, if that square is vacant."
    if(inRange(pawnCell.row + myForward,pawnCell.col) && getCell(pawnCell.row + myForward,pawnCell.col).piece === null) optionalMoves.push(Move(pawnCell, getCell(pawnCell.row + myForward, pawnCell.col)));
    // Rule: "If it has not yet moved, a pawn also has the option of moving two squares straight forward, provided both squares are vacant."
    if(inRange(pawnCell.row + myForward,pawnCell.col) && inRange(pawnCell.row + myForward*2,pawnCell.col) && getCell(pawnCell.row + myForward,pawnCell.col).piece === null && getCell(pawnCell.row + myForward*2,pawnCell.col).piece === null) optionalMoves.push(Move(pawnCell, getCell(pawnCell.row + myForward*2, pawnCell.col)));

    return optionalMoves;
}
//</editor-fold>
