package com.learning.cluster.loadbalance;

import com.learning.cluster.LoadBalance;
import java.util.List;

public abstract class AbstractLoadBalance implements LoadBalance {
    public int getWeight(String ipAdressAndWeight){
        int weight=0;
        String[] ipAdrrAndWeight = ipAdressAndWeight.split(":");
        if(ipAdrrAndWeight.length==3){
            weight = Integer.parseInt(ipAdrrAndWeight[2]);
        }
        return weight;
    }

    @Override
    public String select(List<String> dataList) {
        if(dataList==null || dataList.size()==0){
            return null;
        }
        if(dataList.size()==1){
            return dataList.get(0);
        }
        return doSelect(dataList);
    }

    public abstract String doSelect(List<String> dataList);
}
