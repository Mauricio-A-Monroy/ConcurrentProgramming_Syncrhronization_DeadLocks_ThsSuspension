package edu.eci.arsw.threads;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import  edu.eci.arsw.spamkeywordsdatasource.*;
import  edu.eci.arsw.blacklistvalidator.*;

public class BlackListThread extends Thread{

    private int lowServer;
    private int highServer;
    private AtomicInteger checkedListsCount;
    private String ipAddress;
    private List<Integer> blackListOcurrences;

    private int limit;

    public BlackListThread(int low, int high, String ipAddress, List<Integer> blackListOcurrences, AtomicInteger checkedListsCount, int limit){
        this.lowServer = low;
        this.highServer = high;
        this.ipAddress = ipAddress;
        this.blackListOcurrences = blackListOcurrences;
        this.checkedListsCount = checkedListsCount;
        this.limit = limit;
    }

    public int getLowServer() {
        return lowServer;
    }

    public int getHighServer() {
        return highServer;
    }

    public int getCheckedListsCount(){
        return checkedListsCount.get();
    }

    public int getOcurrencesCount(){
        return blackListOcurrences.size();
    }

    public List<Integer> getBlackList(){
        return blackListOcurrences;
    }

    public String getIpAddress(){
        return ipAddress;
    }

    public void run(){

        HostBlacklistsDataSourceFacade skds=HostBlacklistsDataSourceFacade.getInstance();
        for (int i = this.getLowServer(); i <= this.getHighServer(); i++){
            if (skds.isInBlackListServer(i, this.getIpAddress())){
                synchronized (blackListOcurrences) {
                    blackListOcurrences.add(i);
                }
            }
            synchronized (blackListOcurrences){
                if (blackListOcurrences.size() == limit) break;
            }
            checkedListsCount.getAndIncrement();
        }

        /**
        System.out.println("Low: " + this.lowServer);
        System.out.println("High: " + this.highServer);
        System.out.println("Ocurrences Count: " + this.blackListOcurrences.size());
        System.out.println("Checked Lists: " + this.checkedListsCount);
        **/
    }
}
