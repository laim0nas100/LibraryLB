/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;

/**
 * Custom Task
 * @author Laimonas Beniušis
 */
public abstract class ExtTask extends Task<Void> {
    
    @Override
    protected abstract Void call() throws Exception;
    
    protected String taskDescription;
    protected long refreshDuration = 500;
    public SimpleBooleanProperty paused = new SimpleBooleanProperty(false);
    public Task childTask;
    public String getTaskDescription() {
        return taskDescription;
    }
    
    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public boolean isPaused() {
        return paused.get();
    }

    public void setRefreshDuration(long numb){
        refreshDuration = numb;
    }
    public long getRefreshDuration(){
        return this.refreshDuration;
    }

    
}