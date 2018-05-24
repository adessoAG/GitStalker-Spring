package entities.Level1.Level2.Level3;

import entities.Level1.Level2.Level3.Level4.PageInfo;
import entities.Level1.Level2.Level3.Level4.Nodes;

import java.util.ArrayList;

public class Members {

    private int totalCount;
    private ArrayList<Nodes> nodes;
    private PageInfo pageInfo;

    public Members() {
    }

    public int getTotalCount() {
        return totalCount;
    }

    public PageInfo getPageInfo() {
        return pageInfo;
    }

    public ArrayList<Nodes> getNodes() { return  nodes; }
}
