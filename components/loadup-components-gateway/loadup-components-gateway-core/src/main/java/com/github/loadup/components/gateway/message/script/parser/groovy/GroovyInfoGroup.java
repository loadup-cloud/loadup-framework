package com.github.loadup.components.gateway.message.script.parser.groovy;

import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.Map.Entry;

/**
 * 脚本分组信息，对比缓存已经存在的脚本信息，按照修改、新增、删除进行分组
 */
public class GroovyInfoGroup {

    /**
     * 修改的脚本
     */
    private final List<GroovyInfo> modifiedGroovy = new ArrayList<GroovyInfo>();

    /**
     * 新增的脚本
     */
    private final List<GroovyInfo> addedGroovy = new ArrayList<GroovyInfo>();

    /**
     * 删除的脚本
     */
    private final List<GroovyInfo> deletedGroovy = new ArrayList<GroovyInfo>();

    /**
     *
     */
    public GroovyInfoGroup(List<GroovyInfo> groovyInfos) {
        for (GroovyInfo groovyInfo : groovyInfos) {
            GroovyInfo cacheGroovy = GroovyInnerCache.getByName(groovyInfo.getClassName());

            // 缓存没有，标识新增的脚本
            if (cacheGroovy == null) {
                addedGroovy.add(groovyInfo);
                continue;
            }

            // 跟缓存的脚本代码不一致，标识修改的脚本
            if (!StringUtils.equals(cacheGroovy.getGroovyContent(), groovyInfo.getGroovyContent())) {
                modifiedGroovy.add(groovyInfo);
                continue;
            }
        }

        // 缓存中存在，新的列表不存在，说明已经删除
        Map<String, GroovyInfo> cacheGroovyInfos = GroovyInnerCache.getGroovyInfos();
        Set<Entry<String, GroovyInfo>> entrySet = cacheGroovyInfos.entrySet();
        for (Entry<String, GroovyInfo> entry : entrySet) {
            if (!groovyInfos.contains(entry.getValue())) {
                deletedGroovy.add(entry.getValue());
            }
        }
    }

    /**
     * Getter method for property <tt>modifiedGroovy</tt>.
     */
    public List<GroovyInfo> getModifiedGroovy() {
        return modifiedGroovy;
    }

    /**
     * Getter method for property <tt>addedGroovy</tt>.
     */
    public List<GroovyInfo> getAddedGroovy() {
        return addedGroovy;
    }

    /**
     * Getter method for property <tt>deletedGroovy</tt>.
     */
    public List<GroovyInfo> getDeletedGroovy() {
        return deletedGroovy;
    }

}
