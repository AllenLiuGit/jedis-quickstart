package com.legend.jedis.quickstart;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.HashSet;
import java.util.Set;

/**
 * Hello world!
 *
 */
public class App 
{
    private static final String DEFAULT_CLUSTER_HOST = "172.17.10.68";
    private static final int DEFAULT_CLUSTER_PORT_START = 7000;
    private static final int DEFAULT_CLUSTER_PORT_END = 7006;

    /**
     * 问题一: 可以只初始化任意一个集群节点的信息,将会通过cluster nodes命令找到其他节点,自动发现
     * 问题二: 如果命令操作的是同一个节点,则没有任何问题,例如:{www}hello/{www}abc会被分配到同一个Slot中; 而如果涉及两个节点,例如: hello/abc则会抛出异常:
     * Exception in thread "main" redis.clients.jedis.exceptions.JedisClusterMaxRedirectionsException: Too many Cluster redirections?
     * 问题原因:
     * 第一次Set或者Get可以成功,是由于此时连接是通过参数:DEFAULT_CLUSTER_HOST/DEFAULT_CLUSTER_PORT_START做的连接,因此,没有问题。
     * 一旦发生节点切换时,我们就需要找到其他节点,如何识别呢? 我们识别集群所有节点的方式是,通过cluster nodes节点命令找到所有对应的节点,
     * 而通过cluster nodes找到的节点IP都是127.0.0.1,因此,我们不能通过这个IP来访问Redis集群
     * 解决方案:参考《原创：切换集群节点IP》,将集群节点IP配置成172.17.10.68,原理是配置redis.conf中的bind参数,同时,重新cluster meet节点正确的IP
     * 规避方案:集群配置时,就要将bind参数设置为内网或外网IP;集群创建时,就要指定内网或外网IP,而不是简单的127.0.0.1
     * @param args
     */
    public static void main( String[] args )
    {
        Set<HostAndPort> jedisClusterNotes = new HashSet<HostAndPort>();

        // Add cluster nodes
//        for (int i = DEFAULT_CLUSTER_PORT_START; i < DEFAULT_CLUSTER_PORT_END; i++) {
//            jedisClusterNotes.add(new HostAndPort(DEFAULT_CLUSTER_HOST, i));
//        }
        jedisClusterNotes.add(new HostAndPort(DEFAULT_CLUSTER_HOST, DEFAULT_CLUSTER_PORT_START));
        System.out.println("Init cluster nodes...");
        // Jedis Cluster will attempt to discover cluster nodes automatically

        // Jedis Cluster
        JedisCluster jedisCluster = new JedisCluster(jedisClusterNotes, 2000, 10);
        System.out.println("Init cluster...");

        jedisCluster.set("{www}hello", "www.baidu.com");
        jedisCluster.set("{www}abc", "def");
        jedisCluster.set("hello", "world");
        jedisCluster.set("abc", "kkkkkkkkk");

        String hello = jedisCluster.get("hello");
        String abc = jedisCluster.get("abc");

        System.out.println(hello);
        System.out.println(abc);
    }
}
