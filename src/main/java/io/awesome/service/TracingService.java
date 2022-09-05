package io.awesome.service;

import java.util.Map;
import java.util.function.Supplier;

public interface TracingService {

  void trace(String name, Map<String, String> tags);

  void trace(String name, Map<String, String> tags, Supplier<Map<String, String>> supplier);
}
