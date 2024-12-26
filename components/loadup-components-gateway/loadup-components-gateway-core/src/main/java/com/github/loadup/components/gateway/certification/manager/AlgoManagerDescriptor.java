package com.github.loadup.components.gateway.certification.manager;

/**
 * 算法管理描述
 */
//@XObject("AlgoManagerDesc")
public class AlgoManagerDescriptor {

    /**
     * 组件的名字
     */
    //@XNode("@name")
    private String name;

    //@XNodeSpring("@listener")
    private AlgoManager algoManager;

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("AlgoManagerDescriptor{");
        sb.append("algoManager=").append(algoManager);
        sb.append(", name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }

    /**
     * Getter method for property <tt>algoManager<tt>.
     */
    public AlgoManager getAlgoManager() {
        return algoManager;
    }

    /**
     * Setter method for property <tt>algoManager<tt>.
     */
    public void setAlgoManager(AlgoManager algoManager) {
        this.algoManager = algoManager;
    }

    /**
     * Getter method for property <tt>name<tt>.
     */
    public String getName() {
        return name;
    }

    /**
     * Setter method for property <tt>name<tt>.
     */
    public void setName(String name) {
        this.name = name;
    }
}