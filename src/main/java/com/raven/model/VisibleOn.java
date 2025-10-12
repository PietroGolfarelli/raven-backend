
package com.raven.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * VisibleOn nested object for Product visibility settings
 */
public class VisibleOn {
    
    @JsonProperty("pos")
    private Boolean pos;
    
    @JsonProperty("app")
    private Boolean app;
    
    // Constructors
    public VisibleOn() {
    }
    
    public VisibleOn(Boolean pos, Boolean app) {
        this.pos = pos;
        this.app = app;
    }
    
    // Getters and Setters
    public Boolean getPos() {
        return pos;
    }
    
    public void setPos(Boolean pos) {
        this.pos = pos;
    }
    
    public Boolean getApp() {
        return app;
    }
    
    public void setApp(Boolean app) {
        this.app = app;
    }
    
    @Override
    public String toString() {
        return "VisibleOn{" +
                "pos=" + pos +
                ", app=" + app +
                '}';
    }
}
