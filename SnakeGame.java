import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.LinkedList;

public class SnakeGame extends JFrame{

    public SnakeGame(){
        super("Snake Game");

        Platform platform = new Platform(600, 600, 12);
        setContentPane(platform);
        addKeyListener(platform.keyAdapter);

        setIconImage(new ImageIcon(".\\imgs\\Icon.png").getImage());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        Timer timer = new Timer(32,e -> platform.frame());
        timer.start();
    }

    public static void main(String[] args) { new SnakeGame(); }

}

class Platform extends JPanel {

    public KeyListener keyAdapter = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == 32){
                if (gameOver || finish) {
                    locateFood();
                    snake.dirs = new Direction[snake.maxLength];
                    snake.init(Direction.Up, StartLength, Unit, Height - (StartLength+1) * Unit);
                    gameOver = false;
                    finish = false;
                } else {
                    pause = !pause;
                    repaint();
                }
                return;
            }

            if(!pause && !gameOver) {
                Direction lastDir = (snake.asyncDirs.size() > 0)? snake.asyncDirs.getLast() : snake.dirs[0];
                switch (e.getKeyCode()) {
                    case 37:
                        if (lastDir != Direction.Left && lastDir != Direction.Right)
                            snake.asyncDirs.add(Direction.Left);
                        break;
                    case 38:
                        if (lastDir != Direction.Up && lastDir != Direction.Down)
                            snake.asyncDirs.add(Direction.Up);
                        break;
                    case 39:
                        if (lastDir != Direction.Left && lastDir != Direction.Right)
                            snake.asyncDirs.add(Direction.Right);
                        break;
                    case 40:
                        if (lastDir != Direction.Up && lastDir != Direction.Down)
                            snake.asyncDirs.add(Direction.Down);
                        break;
                }
            }
        }
    };

    private final Random rnd = new Random();
    private final int Width, Height, Unit, StartLength;
    private final Snake snake;
    private int xFood, yFood;
    private boolean gameOver = false, pause = false, finish = false;
    private final BufferedImage head, body1, body2, bodyTurn1_1, bodyTurn1_2, bodyTurn2_1, bodyTurn2_2, tail, tailTurn, tailTurn2;
    public Platform(int Width, int Height, int Unit){
        setPreferredSize(new Dimension(Width, Height));
        setBackground(new Color(0, 30, 9));

        this.Height = Height;
        this.Width = Width;
        this.Unit = Unit;

        try {
            head = ImageIO.read(new File(".\\imgs\\Head.png"));
            body1 = ImageIO.read(new File(".\\imgs\\Body1.png"));
            body2 = ImageIO.read(new File(".\\imgs\\Body2.png"));
            bodyTurn1_1 = ImageIO.read(new File(".\\imgs\\BodyTurn1_1.png"));
            bodyTurn1_2 = ImageIO.read(new File(".\\imgs\\BodyTurn1_2.png"));
            bodyTurn2_1 = ImageIO.read(new File(".\\imgs\\BodyTurn2_1.png"));
            bodyTurn2_2 = ImageIO.read(new File(".\\imgs\\BodyTurn2_2.png"));
            tail = ImageIO.read(new File(".\\imgs\\Tail.png"));
            tailTurn = ImageIO.read(new File(".\\imgs\\TailTurn.png"));
            tailTurn2 = ImageIO.read(new File(".\\imgs\\TailTurn2.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        StartLength = Height/Unit/5;
        snake = new Snake(Direction.Up, StartLength, Unit, Height - (StartLength+1) * Unit);
        locateFood();
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(new Color(8, 255, 0));
        g2.fillOval(xFood,yFood,Unit,Unit);

        AffineTransform oldTx = g2.getTransform();
        if(snake.dirs[snake.length-1] == snake.dirs[snake.length-2]) {
            g2.rotate(getAngle(snake.dirs[snake.length - 1]), snake.x[snake.length - 1] + Unit / 2.0, snake.y[snake.length - 1] + Unit / 2.0);
            g2.drawImage(tail, snake.x[snake.length-1], snake.y[snake.length-1], Unit, Unit, this);
        } else {
            g2.rotate(getAngle(snake.dirs[snake.length - 2]), snake.x[snake.length - 1] + Unit / 2.0, snake.y[snake.length - 1] + Unit / 2.0);
            if(isClockWise(snake.dirs[snake.length-2], snake.dirs[snake.length-1]))
                g2.drawImage(tailTurn, snake.x[snake.length - 1], snake.y[snake.length - 1], Unit, Unit, this);
            else g2.drawImage(tailTurn2, snake.x[snake.length - 1], snake.y[snake.length - 1], Unit, Unit, this);
        }
        g2.setTransform(oldTx);

        int bias = ((((snake.x[0]/Unit)%2 == 1 && (snake.y[0]/Unit)%2 == 1)) || ((snake.x[0]/Unit)%2 == 0 && (snake.y[0]/Unit)%2 == 0))? 1 : 0;
        for(int i = 1; i < snake.length-1; i++){
            if(snake.dirs[i] == snake.dirs[i-1]) {
                g2.rotate(getAngle(snake.dirs[i]), snake.x[i] + Unit / 2.0, snake.y[i] + Unit / 2.0);
                if ((i + bias) % 2 == 0) g2.drawImage(body1, snake.x[i], snake.y[i], Unit, Unit, this);
                else g2.drawImage(body2, snake.x[i], snake.y[i], Unit, Unit, this);
            }else {
                g2.rotate(getAngle(snake.dirs[i-1]), snake.x[i] + Unit / 2.0, snake.y[i] + Unit / 2.0);
                if(isClockWise(snake.dirs[i-1], snake.dirs[i])) {
                    if ((i + bias) % 2 == 0) g2.drawImage(bodyTurn1_1, snake.x[i], snake.y[i], Unit, Unit, this);
                    else g2.drawImage(bodyTurn1_2, snake.x[i], snake.y[i], Unit, Unit, this);
                }
                else {
                    if ((i + bias) % 2 == 0) g2.drawImage(bodyTurn2_2, snake.x[i], snake.y[i], Unit, Unit, this);
                    else g2.drawImage(bodyTurn2_1, snake.x[i], snake.y[i], Unit, Unit, this);
                }
            }
            g2.setTransform(oldTx);
        }

        g2.rotate(getAngle(snake.dirs[0]),snake.x[0]+Unit/2.0, snake.y[0]+Unit/2.0);
        g2.drawImage(head, snake.x[0], snake.y[0], Unit, Unit, this);
        g2.setTransform(oldTx);

        Font font = new Font("Helvetica", Font.BOLD, 12);
        g2.setColor(Color.white);
        g2.setFont(font);
        g2.drawString("Score : " + (snake.length - StartLength), Unit, Unit+12);

        if(gameOver || pause || finish) {
            String msg = (gameOver)? "Game Over" : (finish)? "Congratulation" : "Paused";
            font = new Font("Helvetica", Font.BOLD, 30);
            FontMetrics metr = getFontMetrics(font);
            g2.setFont(font);
            g2.drawString(msg, (Width - metr.stringWidth(msg)) / 2, (Height / 2) + 15);

            msg = "Press Space to " + ((gameOver || finish)? "replay" : "continue");
            font = new Font("Helvetica", Font.BOLD, 14);
            metr = getFontMetrics(font);
            g2.setFont(font);
            g2.drawString(msg, (Width - metr.stringWidth(msg)) / 2, (Height / 2) + 45);
        }
    }

    private boolean isClockWise(Direction dir1, Direction dir2){
        return (dir1 == Direction.Right && dir2 == Direction.Up) ||
                (dir1 == Direction.Left && dir2 == Direction.Down) ||
                (dir1 == Direction.Down && dir2 == Direction.Right) ||
                (dir1 == Direction.Up && dir2 == Direction.Left);
    }

    private double getAngle(Direction direction){
        switch (direction){
            case Up:
                return 0;
            case Down:
                return Math.PI;
            case Left:
                return 1.5*Math.PI;
            default:
                return 0.5*Math.PI;
        }
    }

    private void locateFood(){
        boolean exist = true;
        searchExist: while(exist){
            xFood = rnd.nextInt((Width-Unit)/Unit)*Unit;
            yFood = rnd.nextInt((Height-Unit)/Unit)*Unit;
            for(int i = 0; i < snake.length; i++){
                if(xFood == snake.x[i] && yFood == snake.y[i]) continue searchExist;
            }
            exist = false;
        }
    }

    public void frame(){
        if(!gameOver && !pause && !finish) {
            snake.move();
            repaint();
        }
    }

    private enum Direction { Up, Down, Left, Right }

    private class Snake {
        int length, maxLength;
        int[] x,y;
        Direction[] dirs;
        LinkedList<Direction> asyncDirs = new LinkedList<>();

        public Snake(Direction direction, int length, int xPos, int yPos){
            maxLength = (Width * Height) / (Unit * Unit);
            x = new int[maxLength];
            y = new int[maxLength];
            dirs = new Direction[maxLength];

            init(direction, length, xPos, yPos);
        }

        public void init(Direction direction, int length, int xPos, int yPos){
            this.length = length;
            dirs[0] = direction;

            x[0] = (xPos < 0) ? 0 : Math.min(xPos, Width - Unit);
            y[0] = (yPos < 0) ? 0 : Math.min(yPos, Height - Unit);

            for(int i = 1; i < length; i++) {
                if(dirs[i] == null) dirs[i] = direction;
                int x_inc = (direction == Direction.Left)? Unit : (direction == Direction.Right)? -Unit : 0;
                int y_inc = (direction == Direction.Up)? Unit : (direction == Direction.Down)? -Unit : 0;
                x[i] = (x[i-1] + x_inc < 0) ? Width - Unit : (x[i-1] + x_inc > Width - Unit) ? 0 : x[i-1] + x_inc;
                y[i] = (y[i-1] + y_inc < 0) ? Height - Unit : (y[i-1] + y_inc > Height - Unit) ? 0 : y[i-1] + y_inc;
            }
        }

        public void move(){
            if (x[0] == xFood && y[0] == yFood) {
                length++;
                if(length == maxLength){
                    finish = true;
                    return;
                }
                locateFood();
            }
            for (int i = length-1; i > 0; i--) {
                dirs[i] = dirs[i-1];
                x[i] = x[i - 1];
                y[i] = y[i - 1];
            }

            if (asyncDirs.size() > 0) dirs[0] = asyncDirs.poll();

            int x_temp = (dirs[0] == Direction.Left)? -Unit : (dirs[0] == Direction.Right)? Unit : 0;
            int y_temp = (dirs[0] == Direction.Up)? -Unit : (dirs[0] == Direction.Down)? Unit : 0;
            x_temp = (x[0] + x_temp < 0) ? Width - Unit : (x[0] + x_temp > Width - Unit) ? 0 : x[0] + x_temp;
            y_temp = (y[0] + y_temp < 0) ? Height - Unit : (y[0] + y_temp > Height - Unit) ? 0 : y[0] + y_temp;

            for (int i = length-1; i > 0; i--) {
                if (x[i] == x_temp && y[i] == y_temp) {
                    gameOver = true;
                    return;
                }
            }

            x[0] = x_temp;
            y[0] = y_temp;
        }
    }
}