package rSpawn;


import arc.util.Align;
import arc.util.Timer;
import mindustry.gen.Call;
import static rSpawn.Constants.*;

public class waveInfo {
    volatile boolean doUpdate = false;
    volatile int restTime;
    String unitAmount;

    Runnable spawnFunction;
    boolean hasSpawned = false;

    public waveInfo(){
    }

    public void show(){
        //Call.infoPopup("Dit is info",1.01f, Align.topLeft,200,0,60, 50);
        show(DEFAULTTIME, DEFAULTAMOUNT, ()->{System.out.println("TESTING");});
    }

    public void show(int time, int amount, Runnable r){
        this.restTime = time;
        this.unitAmount = Integer.toString(amount);
        this.spawnFunction = r;
        this.hasSpawned = false;

        if(!doUpdate) {
            this.doUpdate = true;
            this.update();
        }
    }

    private void update(){
        if(this.doUpdate){
            //String msg = String.format(INFOMSG, (int)(restTime/60),restTime%60,"?","?");
            String msg = String.format(MSGAMOUNT, (int)(restTime/60),restTime%60, this.unitAmount);
            Call.infoPopup(msg, 1f, Align.topLeft, 200, 0, 60, 50);
            if(restTime != 0){
                restTime--;
            }else if(!hasSpawned){
                spawnFunction.run();
                // check for errors!
                spawnFunction = null;
                this.unitAmount = "?";
                hasSpawned = true;
            }
            Timer.schedule(()->this.update(), 1f);
        }
    }

    public void disable(){
        this.doUpdate = false;
    }

    public String getStatus(){
        if(!doUpdate){
            return "disabled";
        }else{
            return String.format(
                    "next wave: %d:%d\nenemies: %s\n----------------------",
                    (int)(restTime/60),restTime%60, this.unitAmount);
        }
    }

    public void switchHud(){
        this.doUpdate = !this.doUpdate;
    }

    public void showLeft(){
        Call.infoPopup("Dit is info",10f, Align.left,200,0,60, 50);
    }

    public void showRight(){
        Call.infoPopup("Dit is info",10f, Align.right,200,0,60, 50);
    }

    public void showRigthB(){
        Call.infoPopup("Dit is info",10f, Align.bottomRight,200,0,60, 50);
    }
}
