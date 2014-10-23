package com.auscultator.data;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by hongyan on 2014/10/23.
 */
public class AudioTag {
    private boolean isDir;
    private String name;
    private String label;
    private String path;
    private AudioTag parent;
    private List<AudioTag> children;

    /* Constructor */
    AudioTag(boolean isDir, String name, String label, String path) {
        this.isDir = isDir;
        this.name = name;
        this.label = label;
        this.path = path;

        if (isDir) {
            children = new ArrayList<AudioTag>();
        }
    }

    /* Add child */
    public boolean push(AudioTag node) {
        node.parent = this;
        return (children != null) && children.add(node);
    }

    public boolean isDir() {
        return isDir;
    }

    public void setDir(boolean isDir) {
        this.isDir = isDir;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public AudioTag getParent() {
        return parent;
    }

    public void setParent(AudioTag parent) {
        this.parent = parent;
    }

    public List<AudioTag> getChildren() {
        return children;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
