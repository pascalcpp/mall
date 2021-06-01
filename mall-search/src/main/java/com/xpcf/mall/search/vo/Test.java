package com.xpcf.mall.search.vo;

import java.util.concurrent.*;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 1/14/2021 10:26 PM
 */
public class Test {
    public static ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("hello");
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("end");
            return 1;
        }, executor);

//        System.out.println("before");
//        // will block current thread
//        System.out.println(completableFuture.get());
//        System.out.println("after");
}


}
