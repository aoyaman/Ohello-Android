package io.github.aoyaman;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import java.util.ArrayList;
import java.util.List;

public class Othello {
    private final static String TAG = Othello.class.getSimpleName();

    private SurfaceHolder mSurfaceHolder;

    private String[][] mCells = new String[10][10];

    private float mCellsLeft, mCellsTop, mCellWidth, mCellHeight;

    private final static String CHAR_BLACK = "BLACK";
    private final static String CHAR_WHITE = "WHITE";

    private String mNow = CHAR_BLACK;
    private int[][] mCheckd = new int[10][10];
    private boolean mIsGameOver = false;

    public Othello(SurfaceHolder surfaceHolder) {
        this.mSurfaceHolder = surfaceHolder;

        // 8 x 8 の盤を作る(番兵の列を含めると 10 x 10)
        final String[] alphabet = { "◆", "A", "B", "C", "D", "E", "F", "G", "H", "◆"};

        for(int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                if (y == 0 || y == 9) {
                    mCells[y][x] = alphabet[x];
                } else if (x == 0 || x == 9) {
                    mCells[y][x] = String.valueOf(y);
                } else {
                    mCells[y][x] = null;
                }
            }
        }

        mCells[4][4] = CHAR_BLACK;
        mCells[4][5] = CHAR_WHITE;
        mCells[5][4] = CHAR_WHITE;
        mCells[5][5] = CHAR_BLACK;

        Log.d(TAG, "mCells="+ mCells.toString());

        check();

        draw();

    }

    public void draw() {
        Canvas canvas = mSurfaceHolder.lockCanvas();

        int w = canvas.getWidth(), h = canvas.getHeight();

        // 一旦クロで塗りつぶす
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), new Paint());

        if (w < h) {
            mCellWidth = (w - 100) / 10;
            mCellHeight = mCellWidth;
            mCellsLeft = 50;
            mCellsTop = 50;
        } else {
            mCellHeight = (h - 100) / 10;
            mCellWidth = mCellHeight;
            mCellsLeft = 50;
            mCellsTop = 50;
        }

        // 盤の背景色
        Paint paintBg = new Paint();
        paintBg.setColor(Color.argb(0xff, 0x48, 0x5d, 0x3f));
        canvas.drawRect(mCellsLeft + mCellWidth, mCellsTop + mCellHeight, mCellsLeft + mCellWidth * 9, mCellsTop + mCellHeight * 9, paintBg);

        // 枠線
        Paint paintLine = new Paint();
        paintLine.setColor(Color.WHITE);
        paintLine.setStyle(Paint.Style.STROKE);
        paintLine.setStrokeWidth(8);

        // 文字色
        Paint paintChar = new Paint();
        paintChar.setColor(Color.WHITE);
        paintChar.setTextSize(100);
        paintChar.setTypeface(Typeface.MONOSPACE);

        for(int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {

                if (y != 0 && y != 9 && x != 0 && x != 9) {
                    canvas.drawRect(mCellsLeft + x * mCellWidth,
                            mCellsTop + y * mCellHeight,
                            mCellsLeft + x * mCellWidth + mCellWidth,
                            mCellsTop + y * mCellHeight + mCellHeight,
                            paintLine);
                }

                String str = mCells[y][x];
                if (y == 0 || y == 9 || x == 0 || x == 9) {
                    paintChar.setColor(Color.WHITE);

                } else if (str == null || str.isEmpty()) {
                    paintChar.setColor(Color.GRAY);
                    int point = mCheckd[y][x];
                    if (point > 0) {
                        str = String.valueOf(point);
                    }

                } else if (str.equals(CHAR_BLACK)) {
                    paintChar.setColor(Color.BLACK);
                    str = "●";

                } else if (str.equals(CHAR_WHITE)) {
                    paintChar.setColor(Color.WHITE);
                    str = "●";
                }
                if (str != null) {
                    canvas.drawText(str,
                            mCellsLeft + x * mCellWidth + 15,
                            mCellsTop + y * mCellHeight + mCellHeight - 15,
                            paintChar);
                }
            }
        }

        String message = "";
        if (mIsGameOver) {
            message = "game is over.";
        } else if (mNow.equals(CHAR_BLACK)) {
            message = "black turn";
        } else {
            message = "white turn";
        }
        canvas.drawText(message,
                mCellsLeft,
                mCellsTop + 11 * mCellHeight + mCellHeight - 15,
                paintChar);

        mSurfaceHolder.unlockCanvasAndPost(canvas);

    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() != MotionEvent.ACTION_UP) {
            return true;
        }


        if (mCellsLeft <= ev.getX() && ev.getX() <= (mCellsLeft + mCellWidth * 10) &&
            mCellsTop <= ev.getY() && ev.getY() <= (mCellsTop + mCellHeight * 10)) {

            int x = (int) ((ev.getX() - mCellsLeft) / mCellWidth);
            int y = (int) ((ev.getY() - mCellsTop) / mCellHeight);

            if (mCheckd[y][x] > 0) {
                putStone(x, y);
            }
        }
        return true;
    }
    private void putStone(int x, int y) {
        // ８方向にひっくり返していく
        for(int d = 0; d < DIRECTIONS.length; d++) {
            mCheckd[y][x] += checkLine(DIRECTIONS[d].x, DIRECTIONS[d].y, x, y, true);
        }
        mCells[y][x] = mNow;

        // 次へ
        mNow = mNow.equals(CHAR_BLACK) ? CHAR_WHITE : CHAR_BLACK;

        List<Hand> hands = check();

        // パスの場合
        if (hands.size() == 0) {
            // 次へ
            mNow = mNow.equals(CHAR_BLACK) ? CHAR_WHITE : CHAR_BLACK;
            hands = check();

            // パスの場合
            if (hands.size() == 0) {
                // ゲーム終了
                mIsGameOver = true;
            }

        }

        // 再描画
        draw();

        // CPU操作
        if (mIsGameOver == false && mNow.equals(CHAR_WHITE)) {
            final List<Hand> cpuHands = hands;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    putStone(cpuHands.get(0).getX(), cpuHands.get(0).getY());


                }
            }).start();
        }
    }

    private final static Point[] DIRECTIONS = {
            new Point(-1, -1),      // 左上
            new Point( 0, -1),      // 上
            new Point( 1, -1),      // 右上
            new Point( -1, 0),      // 左
            new Point(1, 0),        // 右
            new Point(-1, 1),       // 左下
            new Point( 0, 1),       // 下
            new Point(1, 1)         // 右下
    };

    private List<Hand> check() {
        List<Hand> hands = new ArrayList<Hand>();
        for (int y = 1; y < 9; y++) {
            for (int x = 1; x < 9; x++) {
                mCheckd[y][x] = 0;
                if (mCells[y][x] == null) {
                    // ８方向に見ていく
                    for(int d = 0; d < DIRECTIONS.length; d++) {
                        mCheckd[y][x] += checkLine(DIRECTIONS[d].x, DIRECTIONS[d].y, x, y, false);
                    }
                    if (mCheckd[y][x] > 0) {
                        hands.add(new Hand(x, y, mCheckd[y][x]));
                    }
                }
            }
        }
        return hands;
    }
    private int checkLine(int dx, int dy, int x, int y, boolean doFlip) {
        final String next = mNow.equals(CHAR_BLACK) ? CHAR_WHITE : CHAR_BLACK;
        int point = 0;

        // 進めて行って次の石がないセル(今の石 or 番兵)まで行く
        while (true) {
            x += dx;
            y += dy;
            if (mCells[y][x] != null && mCells[y][x].equals(next)){
                point++;
                if (doFlip) {
                    mCells[y][x] = mNow;
                }

            } else {
                break;
            }

        } ;

        // 行き着いた先が今の石じゃないと挟んだことにならない
        if (mCells[y][x] == null || mCells[y][x].equals(mNow) == false) {
            return 0;
        }

        return point;

    }


}
