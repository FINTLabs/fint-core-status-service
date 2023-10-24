package no.fintlabs.api;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Service
public class ApiService {
    public HashMap<String, HashMap> getTopic(String topic) {

        HashMap<String, HashMap> orgIdMap = new HashMap<>();
        HashMap<String, List<String>> domainMap = new HashMap<>();

        String[] topicParts = topic.split("\\.");
        String orgId = topicParts[0];
        String[] domainparts = topicParts[3].split("-");
        String[] packages = topicParts[3].split("-");

        domainMap.put(domainparts[0], Collections.singletonList(packages[1]));
        orgIdMap.put(orgId, domainMap);
        return orgIdMap;
    }
}
