package no.fintlabs.topic;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TopicService {

    public Map<String, Map<String, List<String>>> topicCache = new HashMap<>();

    private Map<String, List<String>> domainMap = new HashMap<>();

    public void addToCache(String topic) {
        String orgId = getTopicOrgId(topic);
        String domain = getTopicDomain(topic);
        Map<String,List<String>> packages = getTopicPackages(topic);
        topicCache.put(orgId, packages);
    }

    public String getTopicOrgId(String topic) {
        String[] topicParts = topic.split("\\.");
        return topicParts[0];
    }

    public String getTopicDomain(String topic) {
        String[] topicParts = topic.split("\\.");
        String[] domainParts = topicParts[3].split("-");
        return domainParts[0];
    }

    public Map<String,List<String>> getTopicPackages(String topic) {
        List<String> packageList = new ArrayList<>();
        String[] topicParts = topic.split("\\.");
        String domain = getTopicDomain(topic);
        String[] packages = topicParts[3].split("-");
        Collections.addAll(packageList, packages[1]);
        domainMap.put(domain, packageList);
        return domainMap;
    }

    public Map<String, Map<String, List<String>>> getCache(){
        return topicCache;
    }

}
