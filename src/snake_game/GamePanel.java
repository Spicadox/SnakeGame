package snake_game;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {
    static final int SCREEN_WIDTH = 700;
    static final int SCREEN_HEIGHT = 700;
    static final int UNIT_SIZE = 25;
    static final int GAME_UNITS = (SCREEN_WIDTH*SCREEN_HEIGHT)/UNIT_SIZE;
    final int x[] = new int[GAME_UNITS];
    final int y[] = new int[GAME_UNITS];
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    final BufferedImage icon = ImageIO.read(new File("C:\\Users\\samph\\Documents\\SnakeGame\\src\\resources\\java-python.png"));
    int bodyParts;
    static final int DELAY = 75;
    int applesEaten;
    int appleX;
    int appleY;
    //char direction;
    Direction direction;
    Direction oldDirection;
    boolean restart;
    boolean GAMESTARTED;
    boolean running;
    boolean paused;
    Timer timer;
    Random random;

    JButton jButton;

    /**
     * enum to represent direction of the snake
     */
    enum Direction {
        UP, DOWN, LEFT, RIGHT;
    }


    GamePanel() throws IOException {
        jButton = new JButton();
        this.setLayout(null);
        jButton.setText("Snake AI Mode");
        jButton.setFocusPainted(false);
        jButton.setBounds(SCREEN_WIDTH / 2 - 125, SCREEN_HEIGHT / 2 - 25, 200, 25);
        //jButton.addActionListener(this);
        //jButton.setMnemonic(MouseEvent.MOUSE_CLICKED);

        random = new Random();
        bodyParts = 3;
        applesEaten = 0;
        //direction = 'R';
        direction = Direction.RIGHT;
        restart = false;
        running = false;
        paused= true;
        this.setPreferredSize(new Dimension(SCREEN_WIDTH,800));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        GAMESTARTED = false;

        startGame();
    }

    /**
     * Method that initializes variables to start the game
     */
    public void startGame() {
        newApple();
        running = true;
        restart = false;
        paused= true;
        GAMESTARTED = false;
        timer = new Timer(DELAY, this);
        timer.start();
        //direction = 'R';
        direction = Direction.RIGHT;
        applesEaten = 0;
        bodyParts = 3;
        repaint();
    }

    /**
     * A method that gets the wav file and continuously loop it
     * @throws IOException
     * @throws LineUnavailableException
     * @throws UnsupportedAudioFileException
     */
    public void playBGM() throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        File audio = new File("C:\\Users\\samph\\Documents\\SnakeGame\\src\\resources\\Renai Circulation - [8-bit; VRC6] [16-bit; SNES; Genesis]-uVJ1z38oYxY.wav");
        Clip clip = AudioSystem.getClip();
        clip.open(AudioSystem.getAudioInputStream(audio));
        clip.start();
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    /**
     * Method that executes the draw and gameOver method
     * @param g
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
        if(!running) {
            gameOver(g);
        }
        //commenting this if statement prevents error when eating apple
        //if getting nextPoint for snake cpu is null then game over
        if(nextPoint(getRoute(),snakeParts()) == null) {
            gameOver(g);
        }
    }

    /**
     * Method that draws the snake
     * start screen
     * pause screen
     * and status bar
     * @param g
     */
    public void draw(Graphics g) {
        for(int i = 0; i < SCREEN_HEIGHT/UNIT_SIZE; i++) {
            g.drawLine(i*UNIT_SIZE, 0, i*UNIT_SIZE, SCREEN_HEIGHT);
            g.drawLine(0, i*UNIT_SIZE, SCREEN_WIDTH, i*UNIT_SIZE);
        }
        g.setColor(Color.RED);
        g.fillOval(appleX,appleY,UNIT_SIZE,UNIT_SIZE);

        //draw snake
        for(int i = 0; i < bodyParts; i++) {
            if(i == 0) {
                g.setColor(Color.GREEN);
                g.fillRect(x[i],y[i],UNIT_SIZE,UNIT_SIZE);
            }
            else {
                g.setColor(new Color(45,180,0));
                g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
            }
        }

        //draw start screen
        if(GAMESTARTED == false) {
            jButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    jButton.setVisible(false);
                    startGame();
                    snakeAI();
                    revalidate();
                    repaint();

//                    if(running) {
//                        paused = !paused;
//                        GAMESTARTED = true;
//                    }
                }
            });
            this.add(jButton);

            g.setColor(Color.GREEN);
            Font gameOverFont = new Font("Monotype Corsiva", Font.BOLD, 100);
            g.setFont(gameOverFont);
            g.drawString("Snake Game", 100, SCREEN_HEIGHT/3);

            Font pressSpaceFont = new Font("Serif", Font.BOLD,25);
            g.setFont(pressSpaceFont);
            g.drawString("(Press Space to Start)", SCREEN_WIDTH/3 - 25, SCREEN_HEIGHT/2 - 50);
        }
        else {
            jButton.setVisible(false);
        }

        //draw pause screen
        if(paused && GAMESTARTED) {
            jButton.setVisible(false);
            Font pauseFont = new Font("Serif", Font.BOLD,75);
            g.setColor(Color.WHITE);
            g.setFont(pauseFont);
            g.drawString("Paused", SCREEN_WIDTH/3, SCREEN_HEIGHT/2);
            Graphics2D g2d = (Graphics2D) g;
            Image image = toolkit.getImage("C:\\Users\\samph\\Documents\\SnakeGame\\src\\resources\\worm.gif");
            g2d.drawImage(image, 200, 210 ,100,100, this);
        }

        //draw status bar
        Graphics2D g2d = (Graphics2D) g;
        g.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(10));
        g.drawRect(0, 705, 699, 90);

        Font scoreFont = new Font("Serif", Font.PLAIN, 50);
        g.setFont(scoreFont);
        g.drawString("Score: " + applesEaten, 25, 765);
        g.drawLine(275, 705, 275, 800);
        g.drawImage(icon, 260, 695, 150, 115, null);
        g.drawLine(400, 705, 400, 800);
        g.drawString("Length: " + (applesEaten + 3), 425, 765);
    }

    /**
     * Method that moves the snake
     */
    public void move() {
        //moves the snake body starting from the tail to the next body part
        for(int i = bodyParts; i > 0; i--) {
            x[i] = x[i-1];
            y[i] = y[i-1];
        }

        //move the head forward based on key direction and set the previous direction
        switch(direction) {
            case UP:
                y[0] = y[0] - UNIT_SIZE;
                oldDirection = Direction.UP;
                break;
            case DOWN:
                y[0] = y[0] + UNIT_SIZE;
                oldDirection = Direction.DOWN;
                break;
            case LEFT:
                x[0] = x[0] - UNIT_SIZE;
                oldDirection = Direction.LEFT;
                break;
            case RIGHT:
                x[0] = x[0] + UNIT_SIZE;
                oldDirection = Direction.RIGHT;
                break;
        }
    }

    //move CPU
    public void moveCPU() {
        //moves the snake body starting from the tail to the next body part
        for(int i = bodyParts; i > 0; i--) {
            x[i] = x[i-1];
            y[i] = y[i-1];
        }

        //move the head forward based on key direction and set the previous direction
        switch(direction) {
            case UP:
                y[0] = y[0] - UNIT_SIZE;

                break;
            case DOWN:
                y[0] = y[0] + UNIT_SIZE;

                break;
            case LEFT:
                x[0] = x[0] - UNIT_SIZE;

                break;
            case RIGHT:
                x[0] = x[0] + UNIT_SIZE;

                break;
        }
        /**
         * working rn for cpu
         */
        //double currentDistance = Math.sqrt((appleX - headPoint.getX())*(appleX - headPoint.getX()) + (appleY - headPoint.getY())*(appleY - headPoint.getY()));
        double upDistance = Math.sqrt((appleX - x[0])*(appleX - x[0]) + (appleY - (y[0] + GAME_UNITS))*(appleY - (y[0] + GAME_UNITS)));
        double downDistance = Math.sqrt((appleX - x[0])*(appleX - x[0]) + (appleY - (y[0] - GAME_UNITS))*(appleY - (y[0] - GAME_UNITS)));
        double rightDistance = Math.sqrt((appleX - (x[0] + GAME_UNITS))*(appleX - (x[0] + GAME_UNITS)) + (appleY - y[0])*(appleY - y[0]));
        double leftDistance = Math.sqrt((appleX - (x[0] - GAME_UNITS))*(appleX - (x[0] - GAME_UNITS)) + (appleY - y[0])*(appleY - y[0]));
//        directionDistance.add(upDistance);
//        directionDistance.add(downDistance);
//        directionDistance.add(rightDistance);
//        directionDistance.add(leftDistance);
        double shortestDistance = Math.min(Math.min(upDistance, downDistance), Math.min(rightDistance, leftDistance));

        //if shortest distance is the upDistance
        if(shortestDistance == upDistance) {
            y[0] = y[0] - GAME_UNITS;
        }
        //else if shortest distance is the downDistance
        else if(shortestDistance == downDistance) {
            y[0] = y[0] + GAME_UNITS;
        }
        //else if shortest distance is the rightDistance
        else if(shortestDistance == rightDistance) {
            x[0] = x[0] + GAME_UNITS;
        }
        //else if shortest distance is the leftDistance
        else if(shortestDistance == leftDistance) {
            x[0] = x[0] - GAME_UNITS;
        }
//        else {
//            //if nextPoint can not be found then call gameover in paint method
//        }
    }



//    public void snakeAI() {
//        ArrayList<Point> snakeParts = snakeParts();
//        Point snakeHead = getHead(appleX, appleY);
//        ArrayList<Point> snakeRoute = new ArrayList<>();
//        snakeRoute.add(nextPoint(snakeRoute, snakeParts));
//    }
//    public ArrayList<Point> snakeParts() {
//        ArrayList<Point> snakeParts = new ArrayList<>();
//        Point snakeHead = getHead(appleX, appleY);
//        snakeParts.add(snakeHead);
//
//        //had to subtract 3 to bodyparts or else index error
//        for(int i = bodyParts -3; i > 0; i--) {
//            snakeParts.set(i, new Point(snakeParts.get(i-1).getX(), snakeParts.get(i-1).getY()));
//        }
//        return snakeParts;
//    }
//
//    public ArrayList<Point> getRoute() {
//        ArrayList<Point> route = new ArrayList<>();
//        //Point apple = new Point(appleX,appleY);
//        route.add(nextPoint(route, snakeParts()));
//        return route;
//    }
//
//    public Point getHead(int appleX, int appleY) {
//        Point snakeHead = new Point(0, 0);
//        if(snakeHead.getX() == appleX && snakeHead.getY() == appleY) {
//            snakeHead.setX(appleX);
//            snakeHead.setY(appleY);
//        }
//        return snakeHead;
//    }
//
//    public Point nextPoint(ArrayList<Point> route, ArrayList<Point> snakeParts) {
//        Point headPoint = getHead(appleX, appleY);
//        Point nextPoint = getHead(appleX + GAME_UNITS, appleY);;
////        while(headPoint.getX() != appleX && headPoint.getY() != appleY) {
//            if(((headPoint.getX() + GAME_UNITS) > SCREEN_WIDTH) || ((headPoint.getX() - GAME_UNITS) < 0) || ((headPoint.getY() + GAME_UNITS) < SCREEN_HEIGHT) || ((headPoint.getY() - GAME_UNITS) < 0)
//                    || !route.contains(snakeParts.listIterator())) {
//                /**
//                 * instead of a switch direction do switch distance
//                 * where cases of direction which contains hypothetical points if moved
//                 * and comparing the distance between those cases
//                 * with the shortest or smallest cases being the point/direction that snake should move next
//                 * thus becoming the nextPoint
//                 * distance = Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
//                 */
//                 ArrayList<Double> directionDistance = new ArrayList<Double>(4);
//                //double currentDistance = Math.sqrt((appleX - headPoint.getX())*(appleX - headPoint.getX()) + (appleY - headPoint.getY())*(appleY - headPoint.getY()));
//                double upDistance = Math.sqrt((appleX - headPoint.getX())*(appleX - headPoint.getX()) + (appleY - (headPoint.getY() + GAME_UNITS))*(appleY - (headPoint.getY() + GAME_UNITS)));
//                double downDistance = Math.sqrt((appleX - headPoint.getX())*(appleX - headPoint.getX()) + (appleY - (headPoint.getY() - GAME_UNITS))*(appleY - (headPoint.getY() - GAME_UNITS)));
//                double rightDistance = Math.sqrt((appleX - (headPoint.getX() + GAME_UNITS))*(appleX - (headPoint.getX() + GAME_UNITS)) + (appleY - headPoint.getY() )*(appleY - headPoint.getY()));
//                double leftDistance = Math.sqrt((appleX - (headPoint.getX() - GAME_UNITS))*(appleX - (headPoint.getX() - GAME_UNITS)) + (appleY - headPoint.getY() )*(appleY - headPoint.getY()));
//                directionDistance.add(upDistance);
//                directionDistance.add(downDistance);
//                directionDistance.add(rightDistance);
//                directionDistance.add(leftDistance);
//                double shortestDistance = Math.min(Math.min(upDistance, downDistance), Math.min(rightDistance, leftDistance));
//
//                //if shortest distance is the upDistance
//                if(shortestDistance == directionDistance.get(0)) {
//                    nextPoint.setX(nextPoint.getX());
//                    nextPoint.setY(nextPoint.getY() + GAME_UNITS);
//                }
//                //else if shortest distance is the downDistance
//                else if(shortestDistance == directionDistance.get(1)) {
//                    nextPoint.setX(nextPoint.getX());
//                    nextPoint.setY(nextPoint.getY() - GAME_UNITS);
//                }
//                //else if shortest distance is the rightDistance
//                else if(shortestDistance == directionDistance.get(2)) {
//                    nextPoint.setX(nextPoint.getX() + GAME_UNITS);
//                    nextPoint.setY(nextPoint.getY());
//                }
//                //else if shortest distance is the leftDistance
//                else if(shortestDistance == directionDistance.get(3)) {
//                    nextPoint.setX(nextPoint.getX() - GAME_UNITS);
//                    nextPoint.setY(nextPoint.getY());
//                }
//            }
//            else {
//                //if nextPoint can not be found then call gameover in paint method
//                nextPoint = null;
//            }
////        }
//        return nextPoint;
//    }

    /**
     * Methods that was suppose to use a LinkedList implementation
     */
//    public LinkedList<Point> snakeParts() {
//        LinkedList<Point> snakeParts = new LinkedList<>();
//        Point snakeHead = getHead(appleX, appleY);
//        snakeParts.addFirst(snakeHead);
//
//        for(int i = bodyParts; i > 0; i--) {
//            snakeParts.add(new Point(snakeHead.getX(), snakeParts.get(i).getY());
//        }
//
//        return snakeParts;
//    }
//
//    public LinkedList<Point> getRoute() {
//        LinkedList<Point> route = new LinkedList<>();
//        Point apple = new Point(appleX,appleY);
//        route.addLast(apple);
//
//
//        return route;
//    }
//
//    public Point getHead(int appleX, int appleY) {
//        Point snakeHead = new Point(0, 0);
//        if(snakeHead.getX() == appleX && snakeHead.getY() == appleY) {
//            snakeHead.setX(appleX);
//            snakeHead.setY(appleY);
//        }
//        return snakeHead;
//    }
//
//    public Point nextPoint(LinkedList<Point> route) {
//        route = new LinkedList<>();
//        Point headPoint = getHead(appleX, appleY);
//        Point nextPoint;
//        while(headPoint.getX() != appleX && headPoint.getY() != appleY) {
//            if(((headPoint.getX() + GAME_UNITS) > SCREEN_WIDTH) || ((headPoint.getX() - GAME_UNITS) < 0) || ((headPoint.getY() + GAME_UNITS) < SCREEN_HEIGHT) || ((headPoint.getY() - GAME_UNITS) < 0)
//            || !route.contains()) {
//
//            }
//        }
//
//        return;
//    }

    /**
     * Method that generates a new apple
     */
    public void newApple() {
        //generate the apple in a random location
        appleX = random.nextInt((int)(SCREEN_WIDTH/UNIT_SIZE))*UNIT_SIZE;
        appleY = random.nextInt((int)(SCREEN_HEIGHT/UNIT_SIZE))*UNIT_SIZE;

        //loop through the snake and if the apple has the same coordinate as any of the body parts of the snake
        //then make a new apple
        for(int i = bodyParts; i >= 0; i--) {
            if((appleX == x[i]) && (appleY == y[i])) {
//                appleX = random.nextInt((int)(SCREEN_WIDTH/UNIT_SIZE))*UNIT_SIZE;
//                appleY = random.nextInt((int)(SCREEN_HEIGHT/UNIT_SIZE))*UNIT_SIZE;
                newApple();
            }
        }
    }

    /**
     * Method that checks the apples location and increment snake body based on apples eaten
     */
    public void checkApple() {
        if((x[0] == appleX) && (y[0] ==appleY)) {
            newApple();
            bodyParts++;
            applesEaten = bodyParts - 3;
        }
    }

    /**
     * Method that checks if the snake head hits the walls or its body
     */
    public void checkCollisions() {
        if(!running) {
            timer.stop();
        }
        //checks if head collides with borders
        if((x[0] < 0) || (x[0] >= SCREEN_WIDTH) || (y[0] < 0) || (y[0] >= SCREEN_HEIGHT)) {
            running = false;
        }
        //checks if head collides with body
        for(int i = bodyParts; i > 0; i--) {
            if ((x[0] == x[i]) && (y[0] == y[i])) {
                running = false;
            }
        }
    }

    /**
     * Method that draws the game over screen
     * @param g
     */
    public void gameOver(Graphics g) {
        if(!running) {
            jButton.setVisible(false);
            //draws "Game Over"
            g.setColor(Color.RED);
            Font gameOverFont = new Font("Monotype Corsiva", Font.BOLD, 100);
            g.setFont(gameOverFont);
            g.drawString("GAME OVER", 50, SCREEN_HEIGHT / 3);

            //draws the score and length
            Font scoreFont = new Font("Serif", Font.BOLD, 75);
            g.setFont(scoreFont);
            g.setColor(Color.WHITE);
            g.drawString("Score: " + applesEaten, SCREEN_WIDTH / 3 - 25, SCREEN_HEIGHT / 2);
            g.drawString("Length: " + (applesEaten + 3), SCREEN_WIDTH / 3 - 25, SCREEN_HEIGHT / 2 + 75);

            //draws the restart instruction string
            Font restartFont = new Font("Serif", Font.BOLD, 25);
            g.setFont(restartFont);
            g.setColor(Color.WHITE);
            g.drawString("(Press Enter to Restart)", SCREEN_WIDTH / 3 - 20, SCREEN_HEIGHT / 2 + 150);

            //draws a gif
            Graphics2D g2d = (Graphics2D) g;
            Image image = toolkit.getImage("C:\\Users\\samph\\Documents\\SnakeGame\\src\\resources\\angry_worm.gif");
            g2d.drawImage(image, 200, 210 ,100,100, this);
        }
    }


    /**
     * Method that continuously moves the snake,
     * generate a new apple, and
     * checks for collision
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if(running && !paused && GAMESTARTED && !restart) {
            move();
            checkApple();
            checkCollisions();
        }

//        if(jButton.isSelected()) {
//            jButton.setVisible(false);
//        }
        repaint();
    }


    /**
     * Method that changes direction based on key code
     */
    public class MyKeyAdapter extends KeyAdapter{
        @Override
        public void keyPressed(KeyEvent e) {
            //gets direction based on key code
            switch(e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    if (oldDirection != Direction.RIGHT) {
                        direction = Direction.LEFT;
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    if (oldDirection != Direction.LEFT) {
                        direction = Direction.RIGHT;
                    }
                    break;
                case KeyEvent.VK_UP:
                    if (oldDirection != Direction.DOWN) {
                        direction = Direction.UP;
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    if (oldDirection != Direction.UP) {
                        direction = Direction.DOWN;
                    }
                    break;
            }
            //pauses the game if space us pressed by changing pause and GAMESTARTED
            if(e.getKeyCode() == KeyEvent.VK_SPACE) {
                if(running) {
                    paused = !paused;
                    GAMESTARTED = true;
                }
            }

            //restarts the game if entered is pressed and move the parts outside the border and resetting the snake
            if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                if(running == false) {
                    restart = true;
                    timer.stop();
                    startGame();
                    for (int i = bodyParts; i > 0; i--) {
                        x[i] = bodyParts * -1;
                        y[i] = 0;
                    }
                    x[0] = 0;
                    y[0] = 0;

                    repaint();
                }
            }
        }
    }
}
