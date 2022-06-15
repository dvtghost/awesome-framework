package io.awesome.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RestConfigDto {
    protected String baseUrl;
    protected String searchPath;
    private String pageNumberName;
    private String pageSizeName;
}
