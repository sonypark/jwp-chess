package wooteco.chess.service;

import java.sql.SQLException;

import wooteco.chess.dao.ChessGameDao;
import wooteco.chess.domain.game.Board;
import wooteco.chess.domain.game.ChessGame;
import wooteco.chess.domain.game.Turn;
import wooteco.chess.domain.game.exception.InvalidTurnException;
import wooteco.chess.domain.game.state.Playing;
import wooteco.chess.domain.piece.Position;
import wooteco.chess.domain.piece.exception.NotMovableException;
import wooteco.chess.dto.BoardDto;
import wooteco.chess.dto.ChessGameDto;
import wooteco.chess.dto.ResponseDto;
import wooteco.chess.dto.StatusDto;
import wooteco.chess.dto.TurnDto;

public class ChessService {
    private static final ChessGameDao chessGameDao = new ChessGameDao();

    public ResponseDto createChessRoom() throws SQLException {
        ChessGame chessGame = chessGameDao.save();
        chessGame.start();
        chessGameDao.update(chessGame);
        return new ResponseDto(ResponseDto.SUCCESS, chessGame.getId());
    }

    public ResponseDto restartGame(int chessRoomId) {
        try {
            ChessGame chessGame = chessGameDao.findById(chessRoomId);
            ChessGame newChessGame = new ChessGame(chessGame.getId(), new Playing(Board.create(), Turn.WHITE));
            chessGameDao.update(newChessGame);
            return new ResponseDto(ResponseDto.SUCCESS, chessGame.getId());
        } catch (SQLException e) {
            return new ResponseDto(ResponseDto.FAIL, "존재하지 않는 방 입니다.");
        }
    }

    public ResponseDto movePiece(int chessGameId, Position sourcePosition, Position targetPosition) {
        ChessGame chessGame = null;
        try {
            chessGame = chessGameDao.findById(chessGameId);
            chessGame.move(sourcePosition, targetPosition);
            chessGameDao.update(chessGame);
            return responseChessGame(chessGame);
        } catch (NotMovableException | IllegalArgumentException e) {
            return new ResponseDto(ResponseDto.FAIL, "이동할 수 없는 위치입니다.");
        } catch (InvalidTurnException e) {
            return new ResponseDto(ResponseDto.FAIL, chessGame.turn().getColor() + "의 턴입니다.");
        } catch (SQLException e) {
            return new ResponseDto(ResponseDto.FAIL, "존재하지 않는 방 입니다.");
        }
    }

    public ResponseDto getChessGameById(int chessGameId) {
        try {
            ChessGame chessGame = chessGameDao.findById(chessGameId);
            return responseChessGame(chessGame);
        } catch (SQLException e) {
            return new ResponseDto(ResponseDto.FAIL, "존재하지 않는 방 입니다.");
        }
    }

    public ResponseDto getGameList() throws SQLException {
        return new ResponseDto(ResponseDto.SUCCESS, chessGameDao.selectAll());
    }

    private static ResponseDto responseChessGame(ChessGame chessGame) {
        return new ResponseDto(ResponseDto.SUCCESS,
            new ChessGameDto(new BoardDto(chessGame.board()), new TurnDto(chessGame.turn()),
                new StatusDto(chessGame.status().getWhiteScore(), chessGame.status().getBlackScore(),
                    chessGame.status().getWinner()), chessGame.isFinished()));
    }
}
