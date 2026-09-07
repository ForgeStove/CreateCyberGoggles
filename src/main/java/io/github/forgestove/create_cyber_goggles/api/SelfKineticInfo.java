package io.github.forgestove.create_cyber_goggles.api;
/**
 * 标记「自身输出转速/应力等动能信息」的 {@code KineticBlockEntity} 子类。
 * <p>
 * 实现该接口的方块已在 {@code addToGoggleTooltip} 里自行推送转速等信息，
 * </p>
 * 本模组的通用注入会跳过它们，避免重复显示。
 */
public interface SelfKineticInfo {}
