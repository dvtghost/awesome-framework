package io.awesome.service;

import java.util.Map;

public interface TracingService {

    void trace(String name, Map<String, String> tags);
}
