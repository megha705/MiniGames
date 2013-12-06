/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.kirauks.minigames.brickbreaker.elements.level;

import java.util.ArrayList;
import java.util.Random;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.util.Duration;
import net.kirauks.minigames.brickbreaker.Game;
import net.kirauks.minigames.brickbreaker.elements.Ball;
import net.kirauks.minigames.brickbreaker.elements.Bar;
import net.kirauks.minigames.brickbreaker.elements.Bloc;
import net.kirauks.minigames.brickbreaker.elements.level.LevelDescriptor.LevelDatas;

/**
 *
 * @author Karl
 */
public class Level extends Parent{
    public static final Duration ANIMATION_TIMELINE_TIME = Duration.millis(40);
    
    public static final double BLOCS_FIRST_LINE = 40d;
    public static final double BLOCS_SPACING = 15d;
    
    private enum BallState{
        CATCHED, MOVING;
    }
    
    private Bar bar;
    private double moveBar;
    private Ball ball;
    private BallState ballState;
    private double moveBallX;
    private double moveBallY;
    
    private LevelDescriptor descriptor;
    
    private Timeline timeline;
    private Random random = new Random();
    
    public Level(LevelDatas datas){
        this.bar = new Bar();
        this.ball = new Ball();
        
        this.descriptor = new LevelDescriptor(datas);
        this.initBlocs();
        
        this.resetPositions();
        
        this.getChildren().addAll(this.bar, this.ball);
        
        this.buildTimeline();
    }
    
    private void resetPositions(){
        this.bar.setTranslateX(Game.STAGE_WIDTH / 2d - Bar.SIZE / 2d);
        this.bar.setTranslateY(Game.STAGE_HEIGHT - Bar.BASELINE - Bar.THICKNESS / 2d);
        this.moveBar = 0;
        
        this.ball.setTranslateX(Game.STAGE_WIDTH / 2d);
        this.ball.setTranslateY(Game.STAGE_HEIGHT - Bar.BASELINE - Bar.THICKNESS / 2d - Ball.RADIUS);
        this.ballState = BallState.CATCHED;
        this.moveBallX = (random.nextDouble() - 0.5) * Ball.SPEED_MIN;
        this.moveBallY = -Ball.SPEED_MIN;
    }
    
    private void initBlocs(){
        ArrayList<ArrayList<Bloc>> levelDatas = this.descriptor.getLevelDatas();
        int nbLine = 0;
        for(ArrayList<Bloc> lineDatas : levelDatas){
            int nbBlocs = lineDatas.size();
            double lineWidth;
            if(nbBlocs > 0){
                lineWidth = nbBlocs * Bloc.SIZE + (nbBlocs - 1) * Level.BLOCS_SPACING;
            }
            else{
                lineWidth = 0;
            }
            int nbBloc = 0;
            for(Bloc b : lineDatas){
                b.setTranslateY(Level.BLOCS_FIRST_LINE + ((Level.BLOCS_SPACING + Bloc.THICKNESS) * nbLine));
                b.setTranslateX(((Game.STAGE_WIDTH - lineWidth) / 2d) + ((Bloc.SIZE + Level.BLOCS_SPACING) * nbBloc));
                this.getChildren().add(b);
                nbBloc++;
            }
            nbLine++;
        }
    }
    
    private void buildTimeline(){
        this.timeline = new Timeline();
        this.timeline.setCycleCount(Timeline.INDEFINITE);
        
        KeyFrame k = new KeyFrame(ANIMATION_TIMELINE_TIME, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                //Bar moves
                if(Level.this.moveBar != 0){
                    Level.this.moveBar();
                }
                
                //Ball moves
                if(Level.this.ballState == BallState.MOVING){
                    Level.this.moveBall();
                }
            }
        });
        this.timeline.getKeyFrames().add(k);
    }
    
    private void moveBar(){
        double toX = this.bar.getTranslateX() + this.moveBar;
        if(toX < 0){
            toX = 0;
        }
        double barMax = Game.STAGE_WIDTH - Bar.SIZE;
        if(toX > barMax){
            toX = barMax;
        }
        if(this.ballState == BallState.CATCHED){
            double ballX = this.ball.getTranslateX() + toX - this.bar.getTranslateX();
            if(ballX < Ball.RADIUS){
                ballX = Ball.RADIUS;
            }
            double ballMax = Game.STAGE_WIDTH - Ball.RADIUS;
            if(ballX > ballMax){
                ballX = ballMax;
            }
            this.ball.setTranslateX(ballX);
        }
        this.bar.setTranslateX(toX);
    }
    
    public void stopMoveBar(){
        this.moveBar = 0d;
    }
    
    public void moveBarLeft(){
        this.moveBar = -Bar.SPEED;
    }
    
    public void moveBarRight(){
        this.moveBar = Bar.SPEED;
    }
    
    private void moveBall(){
        double newX = this.ball.getTranslateX() + this.moveBallX;
        double newY = this.ball.getTranslateY() + this.moveBallY;
        boolean reverseX = false;
        boolean reverseY = false;
        
        //Borders collisions
        if(newX < Ball.RADIUS){
            newX = Ball.RADIUS;
            reverseX = true;
        }
        double ballMaxX = Game.STAGE_WIDTH - Ball.RADIUS;
        if(newX > ballMaxX){
            newX = ballMaxX - (newX - ballMaxX);
            reverseX = true;
        }
        if(newY < Ball.RADIUS){
            newY = Ball.RADIUS;
            reverseY = true;
        }
        double ballMaxY = Game.STAGE_HEIGHT - Ball.RADIUS;
        if(newY > ballMaxY){
            newY = ballMaxY - (newY - ballMaxY);
            reverseY = true;
        }
        
        //Bar collisions
        if(this.moveBallY > 0 
                && newY - Ball.RADIUS >= Game.STAGE_HEIGHT - Bar.BASELINE - Bar.THICKNESS
                && newY - Ball.RADIUS <= Game.STAGE_HEIGHT - Bar.BASELINE //+ Bar.THICKNESS
                && this.bar.getTranslateX() - Ball.RADIUS < newX
                && this.bar.getTranslateX() + Bar.SIZE + Ball.RADIUS > newX){
            reverseY = true;
            //X deviation
            double offset = newX + Ball.RADIUS - this.bar.getTranslateX() - (Bar.SIZE / 2d);
            this.moveBallX += offset / 5d;
        }
        
        this.ball.setTranslateX(newX);
        this.ball.setTranslateY(newY);
        
        if(reverseX){
            this.moveBallX = -this.moveBallX;
        }
        if(reverseY){
            this.moveBallY = -this.moveBallY;
        }
        
        //Blocs colisions
        double ballAjustedY = newY - Level.BLOCS_FIRST_LINE;
        if(ballAjustedY > 0){
            int nbBlocsLines = this.descriptor.getLevelDatas().size();
            double nbBallLine = ballAjustedY / (Level.BLOCS_SPACING + Bloc.THICKNESS);
            if(nbBallLine < nbBlocsLines){
                ArrayList<Bloc> targetLineDatas = this.descriptor.getLevelDatas().get((int)nbBallLine);
                int nbBlocs = targetLineDatas.size();
                double lineWidth;
                if(nbBlocs > 0){
                    lineWidth = nbBlocs * Bloc.SIZE + (nbBlocs - 1) * Level.BLOCS_SPACING;
                }
                else{
                    lineWidth = 0;
                }
                double ballAjustedX = newX - (Game.STAGE_WIDTH - lineWidth) / 2d;
                double ndBallCol = ballAjustedX / (Bloc.SIZE + Level.BLOCS_SPACING);
                if(ndBallCol >= 0 && ndBallCol < targetLineDatas.size()){
                    Bloc target = targetLineDatas.get((int)ndBallCol);
                    //Target bloc colisions
                    if(newX > target.getTranslateX()
                            && newX < target.getTranslateX() + Bloc.SIZE
                            && newY > target.getTranslateY()
                            && newY < target.getTranslateY() + Bloc.THICKNESS){
                       target.destroy();
                    }
                }
            }
        }
    }
    
    public void launchBall(){
        this.ballState = BallState.MOVING;
    }
    
    public void start(){
        this.timeline.play();
    }
    
    public void pause(){
        this.timeline.pause();
    }
    
    public void resume(){
        this.timeline.play();
    }
}
