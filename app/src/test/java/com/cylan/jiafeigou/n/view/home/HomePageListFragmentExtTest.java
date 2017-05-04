package com.cylan.jiafeigou.n.view.home;

import com.cylan.jiafeigou.utils.RandomUtils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Created by hds on 17-5-4.
 */
public class HomePageListFragmentExtTest {

    @Test
    public void testCollections() {
        List<Integer> listA = new ArrayList<>(new HashSet<>(createRandomList()));
        List<Integer> listB = new ArrayList<>(new HashSet<>(createRandomList()));
        Collections.sort(listA);
        Collections.sort(listB);
        System.out.println("listA:" + listA);
        System.out.println("listB:" + listB);
        List<Integer> toRemove = getRetainList(listA, listB);
        System.out.println("交集:" + toRemove);
        System.out.println("差集:" + getDiffList(listA, listB));
        System.out.println("差集:" + getDiffList(listB, listA));
    }

    private List<Integer> createRandomList() {
        int count = RandomUtils.getRandom(5, 10);
        List<Integer> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(RandomUtils.getRandom(20));
        }
        return list;
    }

    private List<Integer> getMergeList(List<Integer> listA, List<Integer> listB) {
        listA.addAll(listB);
        return listA;
    }

    private List<Integer> getDiffList(List<Integer> listA, List<Integer> listB) {
        List<Integer> rawList = new ArrayList<>(listA);
        rawList.removeAll(listB);
        return rawList;
    }

    private List<Integer> getRetainList(List<Integer> listA, List<Integer> listB) {
        List<Integer> rawList = new ArrayList<>(listA);
        rawList.retainAll(listB);
        return rawList;
    }

}