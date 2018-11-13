package com.hzchendou.model.seed;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * DNS发现服务.
 *
 * @author hzchendou
 * @date 18-11-13
 * @since 1.0
 */
public class DNSDiscovery {

    protected final List<DnsSeedDiscovery> seeds;

    private volatile ExecutorService vThreadPool;

    public static final String[] DNS_SEEDS = new String[] {"seed.bitcoin.sipa.be",         // Pieter Wuille
            "dnsseed.bluematt.me",          // Matt Corallo
            "dnsseed.bitcoin.dashjr.org",   // Luke Dashjr
            "seed.bitcoinstats.com",        // Chris Decker
            "seed.bitcoin.jonasschnelli.ch",// Jonas Schnelli
            "seed.btc.petertodd.org",       // Peter Todd
            "seed.bitcoin.sprovoost.nl",    // Sjors Provoost
            "seed.bitnodes.io",             // Addy Yeow
    };

    /**
     * 获取种子节点
     *
     * @param seeds
     */
    public DNSDiscovery(List<DnsSeedDiscovery> seeds) {
        this.seeds = seeds;
    }


    public InetSocketAddress[] getPeers(final long services, final long timeoutValue, final TimeUnit timeoutUnit) {
        vThreadPool = createExecutor();
        try {
            List<Callable<InetSocketAddress[]>> tasks = new ArrayList();
            for (final DnsSeedDiscovery seed : seeds) {
                tasks.add(new Callable<InetSocketAddress[]>() {
                    @Override
                    public InetSocketAddress[] call() throws Exception {
                        return seed.getPeers(services, timeoutValue, timeoutUnit);
                    }
                });
            }
            final List<Future<InetSocketAddress[]>> futures = vThreadPool.invokeAll(tasks, timeoutValue, timeoutUnit);
            ArrayList<InetSocketAddress> addrs = new ArrayList();
            for (int i = 0; i < futures.size(); i++) {
                Future<InetSocketAddress[]> future = futures.get(i);
                if (future.isCancelled()) {
                    continue;  // Timed out.
                }
                final InetSocketAddress[] inetAddresses;
                try {
                    inetAddresses = future.get();
                } catch (ExecutionException e) {
                    continue;
                }
                Collections.addAll(addrs, inetAddresses);
            }
            if (addrs.size() == 0)
                throw new RuntimeException(
                        "No peer discovery returned any results in " + timeoutUnit.toMillis(timeoutValue)
                                + "ms. Check internet connection?");
            Collections.shuffle(addrs);
            vThreadPool.shutdownNow();
            return addrs.toArray(new InetSocketAddress[addrs.size()]);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            vThreadPool.shutdown();
        }
    }


    protected ExecutorService createExecutor() {
        return Executors.newFixedThreadPool(seeds.size());
    }

    /**
     * 默认DNS对象
     *
     * @return
     */
    public static DNSDiscovery defaultDnsDiscovery() {
        return new DNSDiscovery(buildDiscoveries(DNS_SEEDS));
    }

    private static List<DnsSeedDiscovery> buildDiscoveries(String[] seeds) {
        List<DnsSeedDiscovery> discoveries = new ArrayList<>();
        if (seeds != null)
            for (String seed : seeds)
                discoveries.add(new DnsSeedDiscovery(8333, seed));
        return discoveries;
    }

    public static void main(String[] args) {
        InetSocketAddress[] seedAddrs = DNSDiscovery.defaultDnsDiscovery().getPeers(0, 5000, TimeUnit.MILLISECONDS);
        System.out.format("发现种子地址：%s", seedAddrs.length);
    }
}
