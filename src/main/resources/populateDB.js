importPackage(Packages.schema);

var fenEvent = bp.EventSet("", function (e) {
    return e.name.equals("ParseFen");
});

function registerCellsQueries()
{
    for (var i = 0; i < 8; i++)
    {
        for (var j = 0; j < 8; j++)
        {
            CTX.registerParameterizedContextQuery("SpecificCell", "Cell[" + i + "," + j + "]", {
                "row": i,
                "col": j
            });

        }
    }
}

registerCellsQueries();

var doneEvent = bp.EventSet("", function (e) {
    return e.name.equals("Done Populate");
});

function getCell(i,j){
    return CTX.getContextInstances("Cell["+i+","+j+"]").get(0);
}

bp.registerBThread("Test",function ()
{
    bp.sync({waitFor:doneEvent});

    var c = CTX.getContextInstances("NotEmptyCell").get(0);
    bp.log.info(c);

    bp.sync({ request: CTX.UpdateEvent("UpdateCell",{cell:c, piece: null})});

    bp.log.info(CTX.getContextInstances("NotEmptyCell").get(0));
});

bp.registerBThread("Populate",function ()
{
    // cells
    var board = [];
    for(var row = 0; row < 8; row++)
    {
        var r = [];

        for(var col = 0; col < 8; col++)
        {
            var cell = new Cell(row,col);
            bp.sync({ request: CTX.InsertEvent(cell)});
            r.push(cell);
        }
        board.push(r);
    }

    // pieces
    while (true)
    {
        var toParse = bp.sync({waitFor:fenEvent}).data;

        // delete old

        // populate new
        parseBoard(toParse.toString(),board);

        bp.sync({request:bp.Event("Done Populate",board)});
    }
});


function parseBoard(toParse)
{
    var tokens = toParse.split("/");
    var row = 0,column = 0;

    for(var i = 0; i < tokens.length; i++)
    {
        for(var token = 0; token < tokens[i].length(); token++)
        {
            var currentToken = tokens[i].substring(token,token+1);
            var toNum = parseInt(currentToken);

            if(isNaN(toNum))
            {
                var piece = null;
                switch(String(currentToken))
                {
                    case "p": piece = new Piece(Piece.Type.Pawn,Piece.Color.White); break;
                    case "n": piece = new Piece(Piece.Type.Knight,Piece.Color.White); break;
                    case "b": piece = new Piece(Piece.Type.Bishop,Piece.Color.White); break;
                    case "r": piece = new Piece(Piece.Type.Rook,Piece.Color.White); break;
                    case "q": piece = new Piece(Piece.Type.Queen,Piece.Color.White); break;
                    case "k": piece = new Piece(Piece.Type.King,Piece.Color.White); break;
                    case "P": piece = new Piece(Piece.Type.Pawn,Piece.Color.Black); break;
                    case "N": piece = new Piece(Piece.Type.Knight,Piece.Color.Black); break;
                    case "B": piece = new Piece(Piece.Type.Bishop,Piece.Color.Black); break;
                    case "R": piece = new Piece(Piece.Type.Rook,Piece.Color.Black); break;
                    case "Q": piece = new Piece(Piece.Type.Queen,Piece.Color.Black); break;
                    case "K": piece = new Piece(Piece.Type.King,Piece.Color.Black); break;
                }

                if(piece != null)
                {
                    // update cell to store piece
                    bp.sync({ request: CTX.InsertEvent(piece)});
                    bp.sync({ request: CTX.UpdateEvent("UpdateCell",{cell:getCell(row,column++), piece: piece})});
                }
                else column++;
            }
            else column++;
        }

        column = 0;
        row++;
    }
}