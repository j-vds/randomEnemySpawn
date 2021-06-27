package rSpawn;

import arc.*;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Timer;

import mindustry.content.Fx;
import mindustry.content.UnitTypes;
import mindustry.game.EventType.*;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.mod.Plugin;
import mindustry.Vars;
import mindustry.type.UnitType;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.CoreBlock;


import static rSpawn.Constants.*;

public class randomEnemySpawn extends Plugin {
    Seq<Tile> posLocations = new Seq<Tile>();
    boolean draw = false;
    waveInfo wI = new waveInfo();
    int unitAmount = DEFAULTAMOUNT;
    private Timer.Task nextWave = Timer.schedule(()->{}, 0f);

    boolean PVPTEST = false;

    boolean hardDisable = false;

    //register event handlers and create variables in the constructor
    public randomEnemySpawn(){
        //after players joins
        Events.on(PlayerJoin.class, event -> {
            //give info
            event.player.sendMessage("[orange]<RES> active[] - more info use /res");
        });

        Events.on(GameOverEvent.class, event-> {
           disableMode();
        });

        Events.on(WorldLoadEvent.class, event -> {
            //TODO: other thead?
            posLocations.clear();
            // First get all cores
            Seq<Tile> coreTiles = new Seq<>();

            for(CoreBlock.CoreBuild cb: Vars.state.rules.defaultTeam.cores()){
                coreTiles.add(cb.tileOn());
            }

            int tmpWidth = Vars.world.width() - BORDEROFFSET;
            int tmpHeight = Vars.world.height() - BORDEROFFSET;

            boolean inRange;
            for(Tile t : Vars.world.tiles){
                if(t.solid()) continue;
                if(t.centerX() < BORDEROFFSET || t.centerY() < BORDEROFFSET) continue;
                if(t.centerX() > tmpWidth || t.centerY() > tmpHeight) continue;
                inRange = false;
                for(Tile ct:coreTiles){
                    if(t.within(ct,MINCOREDIST*8)){
                        inRange = true;
                        break;
                    }
                }
                if(!inRange) {
                    posLocations.add(t);
                }
            }
            Log.info("<RES> checking tiles done...");
            unitAmount = DEFAULTAMOUNT;
            if(!hardDisable) {
                wI.show(DEFAULTFIRSTWAVE, unitAmount, () -> spawnUnits(unitAmount));
                unitAmount++;
            }

        });

        Log.info("<RES> randomEnemySpawn plugin loaded...");
    }

    //register commands that run on the server
    @Override
    public void registerServerCommands(CommandHandler handler){
        handler.register("res_info","current status", (args)->{
            StringBuilder sb = new StringBuilder();
            sb.append("\n---- RES STATUS ----\n");
            sb.append(wI.getStatus());
            Log.info(sb.toString());
        });

        handler.register("res", "[on/off]", "Turn RES on or off", (args)->{
           if(args.length == 0){
               Log.info((hardDisable)? "<RES> hard-disabled" : "<RES> hard-enabled");
           }else{
               switch (args[0]){
                   case "on":
                   case "1":
                       hardDisable = false;
                       Log.info("<RES> enabled...");
                       break;
                   case "off":
                   case "0":
                       hardDisable = true;
                       Log.info("<RES> disabled ...");
                       break;
                   // testing
                   case "dbgpvp":
                       PVPTEST = !PVPTEST;
                       Log.info("PVPTESTING: @", PVPTEST);
                       return;
                   case "spawnnow":
                       if(wI.restTime > 1){
                           wI.restTime = 5;
                       }
                       return;
                   default:
                       Log.info("<RES> Use \"res on/off\"");
                       return;
               }
               if(hardDisable){
                   disableMode();
               }else if(!nextWave.isScheduled() && !wI.doUpdate){
                   final int a = unitAmount;
                   wI.show(DEFAULTFIRSTWAVE, a, () -> spawnUnits(a));
                   unitAmount++;
                   Call.sendMessage("[orange]<RES>[] RandomEnemySpawn enabled");
               }
           }
        });
    }

    //register commands that player can invoke in-game
    @Override
    public void registerClientCommands(CommandHandler handler) {
        handler.<Player>register("res", "more info about randomEnemySpawn", (args, player)->{
           Call.infoMessage(player.con, HELPMSG);
        });
        /*
        handler.<Player>register("resdebug", "[scarlet]Don't use can lag server[]",(args, player)->{
            wI.show();
            if(!draw) {
                draw = true;
                Timer.schedule(this::drawEffect, 0f);
            }else{
                draw = false;
            }
        });
         */

        handler.<Player>register("spawnenemy", "[scarlet](A)[]Spawns an enemy on random location", (args, player)->{
            if(!player.admin()) return;
            Unit unit;
            if(Mathf.random(10) > 7) {
                unit = UnitTypes.flare.create(Vars.state.rules.waveTeam);
            }else{
                unit = UnitTypes.dagger.create(Vars.state.rules.waveTeam);
                }
            int spread = 10;
            Tile t = posLocations.random();
            unit.set(t.worldx() + Mathf.range(spread), t.worldy() + Mathf.range(spread));
            unit.add();
        });

        handler.<Player>register("resdisable", "[scarlet](A)[]Disable RES", (args, player)->{
            if(!player.admin()) return;
            disableMode();
            Log.info("<RES> disabled by " + Strings.stripColors(player.name));
        });

    }

    private void disableMode(){
        if(nextWave.isScheduled()){
            nextWave.cancel();
        }
        wI.disable();
        Call.sendMessage("[orange]<RES>[] RandomEnemySpawn disabled...");
    }

    private void drawEffect(){
        for(Tile t: posLocations){
            Call.effect(Fx.heal, t.drawx(), t.drawy(), 0, Color.blue);

        }
        if(draw){
            Timer.schedule(this::drawEffect, 0.2f);
        }
    }

    public void spawnUnits(int amount){
        System.out.println(amount);
        System.out.println(unitAmount);
        Unit unit, unitpvp;
        UnitType type = null; //compileError
        Tile t = posLocations.random();
        //pvp stuff
        int xoffset = Vars.world.width()/2 - t.centerX();
        int yoffset = Vars.world.height()/2 - t.centerY();
        Tile t_pvp = Vars.world.tile((int)(Vars.world.width()/2) + xoffset, (int)(Vars.world.height()/2) + yoffset);
        int spread = 16;
        boolean onlyAir = false;
        if(Vars.world.tile(t.pos()).solid() || t.floor().isLiquid){
            onlyAir = true;
            type = UnitTypes.flare;
        }

        if(Vars.state.rules.pvp || PVPTEST) {
           if (Vars.world.tile(t_pvp.pos()).solid() || t_pvp.floor().isLiquid) {
                onlyAir = true;
                type = UnitTypes.flare;
            }
        }

        for(int i=0; i<amount; i++){
            if(!onlyAir){
                type = (Math.random() > 0.72)? UnitTypes.flare:UnitTypes.dagger;
            }
            //if pvp spawn a team that couldn't be in the editor
            unit = type.create((Vars.state.rules.pvp) ? Team.all[8] :Vars.state.rules.waveTeam);
            unit.set(t.worldx() + Mathf.range(spread), t.worldy() + Mathf.range(spread));
            unit.add();
            if(Vars.state.rules.pvp || PVPTEST){
                unitpvp = type.create(Team.all[8]);
                unitpvp.set(t_pvp.worldx() + Mathf.range(spread), t.worldy() + Mathf.range(spread));
                unitpvp.add();
            }
        }

        if(!Vars.state.rules.pvp && !PVPTEST) {
            Call.sendMessage(String.format("[orange]<RES>[] Enemies spawned at: (%d,%d)", t.centerX(), t.centerY()));
            Log.info(String.format("<RES> %d enemies spawned at (%d,%d)", amount, t.centerX(), t.centerY()));
        } else {
            Call.sendMessage(String.format("[orange]<RES>[] Enemies spawned at: (%d,%d) and (%d,%d)", t.centerX(), t.centerY(), t_pvp.centerX(), t_pvp.centerY()));
            Log.info(String.format("<RES> %d enemies spawned at (%d,%d) and (%d,%d)",amount, t.centerX(), t.centerY(), t_pvp.centerX(), t_pvp.centerY()));
        }

        final int a = unitAmount;
        nextWave = Timer.schedule(()->{wI.show(DEFAULTTIME, a, ()->spawnUnits(a));}, DEFAULTBETWEENWAVES);
        unitAmount++;
    }
}
