package wooteco.chess;

import wooteco.chess.controller.WebChessRestController;
import wooteco.chess.service.ChessService;

public class SparkChessApplication {
    public static void main(String[] args) {
        new WebChessRestController(new ChessService());
    }
}
