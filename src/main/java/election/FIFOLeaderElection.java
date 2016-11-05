package election;

import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by mhw on 2016/11/5.
 */
public class FIFOLeaderElection {

    private String zkHost;
    private String zkPath;

    private ZooKeeper zk;

    public FIFOLeaderElection(String zkHost,String zkPath) {
        this.zkPath = zkPath;
        this.zkHost = zkHost;
        try {
            zk = new ZooKeeper(zkHost,10000, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    synchronized public void syncLeaderElection(){

    }

    public boolean asyncLeaderElection(){
        try {

            final String myName = zk.create(zkPath + "/node-",null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            System.out.println("--myname:" + myName);
            List<String> children = zk.getChildren(zkPath,false);
            for(String child : children){
                System.out.println("--child:" + child);
            }

            Collections.sort(children);

            if(myName.equals(children.get(0))){
                return true; // leader
            }else{
                //watch pre znode
                int i = children.indexOf(myName);
                int preIndex = i - 1;
                zk.exists(zkPath + "/" + children.get(preIndex) ,new Watcher(){
                    @Override
                    public void process(WatchedEvent watchedEvent) {
                        List<String> children = null;
                        try {
                            children = zk.getChildren(zkPath,false);
                            for(String child : children){
                                System.out.println("--child:" + child);
                            }
                            Collections.sort(children);

                            if(myName.equals(children.get(0))) {
                                System.out.println("I am leader!");
                            }else{
                                int i = children.indexOf(myName);
                                int preIndex = i - 1;
                                zk.exists(zkPath + "/" + children.get(preIndex) ,this);
                            }
                        } catch (KeeperException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }


    public static void main(String[] args) {
//        String zkHost = "192.168.147.48:2181";
//        String zkPath = "/chroot";
//        FIFOLeaderElection election = new FIFOLeaderElection(zkHost,zkPath);
//        election.asyncLeaderElection();
        List<String> list = new ArrayList<String>();
        list.add("000");
        list.add("002");
        list.add("001");
        Collections.sort(list);
        System.out.println(list);
    }
}
