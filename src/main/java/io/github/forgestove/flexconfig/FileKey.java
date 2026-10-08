package io.github.forgestove.flexconfig;
/** (模组 ID, 配置文件名) 二元组，作为多配置场景下的索引键。 */
public record FileKey(String modId, String fileName) {}
