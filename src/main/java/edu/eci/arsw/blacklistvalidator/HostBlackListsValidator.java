/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;
import edu.eci.arsw.threads.BlackListThread;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;

/**
 *
 * @author hcadavid
 */
public class HostBlackListsValidator {

    private static final int BLACK_LIST_ALARM_COUNT=5;
    
    /**
     * Check the given host's IP address in all the available black lists,
     * and report it as NOT Trustworthy when such IP was reported in at least
     * BLACK_LIST_ALARM_COUNT lists, or as Trustworthy in any other case.
     * The search is not exhaustive: When the number of occurrences is equal to
     * BLACK_LIST_ALARM_COUNT, the search is finished, the host reported as
     * NOT Trustworthy, and the list of the five blacklists returned.
     * @param ipaddress suspicious host's IP address.
     * @return  Blacklists numbers where the given host's IP address was found.
     */
    public List<Integer> checkHost(String ipaddress, int n){

        // Getting the amount of servers and creating array of threads
        HostBlacklistsDataSourceFacade skds=HostBlacklistsDataSourceFacade.getInstance();
        int serverAmount = skds.getRegisteredServersCount();
        List<BlackListThread> threads = new ArrayList<BlackListThread>();
        List<Integer> blackListOcurrences = new ArrayList<>();
        AtomicInteger checkedListsCount = new AtomicInteger(0);

        // Range of servers that will be assigned to each thread
        int threadRange = serverAmount / n;

        // Assigning the range to each thread and checking if the amount is odd or even
        if(n % 2 == 0){
            for(int i = 0; i < n; i++){
                threads.add(new BlackListThread(i * threadRange, ((i+1) * threadRange) - 1, ipaddress, blackListOcurrences, checkedListsCount, BLACK_LIST_ALARM_COUNT));
                threads.get(i).start();
            }
        }
        else {
            for(int i = 0; i < n - 1; i++){
                threads.add(new BlackListThread(i * threadRange, ((i+1) * threadRange) - 1, ipaddress, blackListOcurrences, checkedListsCount,BLACK_LIST_ALARM_COUNT));
                threads.get(i).start();
            }
            threads.add(new BlackListThread((n-1) * threadRange, serverAmount - 1, ipaddress, blackListOcurrences, checkedListsCount, BLACK_LIST_ALARM_COUNT));
            threads.get(n-1).start();
        }

        for(int i = 0; i < n; i++){
            try {
                threads.get(i).join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // Reporting or not the ip address
        if (blackListOcurrences.size() >= BLACK_LIST_ALARM_COUNT){
            skds.reportAsNotTrustworthy(ipaddress);
        }
        else{
            skds.reportAsTrustworthy(ipaddress);
        }

        LOG.log(Level.INFO, "Checked Black Lists:{0} of {1}", new Object[]{checkedListsCount, skds.getRegisteredServersCount()});
        return blackListOcurrences;
    }
    
    
    private static final Logger LOG = Logger.getLogger(HostBlackListsValidator.class.getName());
    
    
    
}
