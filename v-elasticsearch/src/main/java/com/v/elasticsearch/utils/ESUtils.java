package com.v.elasticsearch.utils;

import com.v.core.base.exception.VServerException;
import com.v.core.base.utils.BeanUtils;
import com.v.core.base.utils.JsonUtils;
import com.v.elasticsearch.api.ESDoc;
import com.v.elasticsearch.constants.ESConstant;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author angry_beard
 * @date 2021/7/7 10:42 上午
 */
@Slf4j
@Component
@AllArgsConstructor
public class ESUtils {

    private final RestHighLevelClient restHighLevelClient;

    public boolean createIdx(String idx, Map<String, Map<String, Object>> properties) {
        if (isExistIdx(idx)) {
            return true;
        }
        boolean result = false;
        XContentBuilder builder;
        CreateIndexResponse resp;
        try {
            builder = XContentFactory.jsonBuilder()
                    .startObject()
                    .startObject("mappings")
                    .field("properties", properties)
                    .endObject()
                    .startObject("settings")
                    .field("number_of_shards", ESConstant.DEFAULT_SHARDS)
                    .field("number_of_replicas", ESConstant.DEFAULT_REPLICAS)
                    .endObject()
                    .endObject();
            CreateIndexRequest req = new CreateIndexRequest(idx).source(builder);
            resp = restHighLevelClient.indices().create(req, RequestOptions.DEFAULT);
            result = resp.isAcknowledged();
        } catch (IOException e) {
            log.error("创建索引失败,detail:", e);
        }
        return result;
    }

    public boolean isExistIdx(String idx) {
        GetIndexRequest req = new GetIndexRequest(idx);
        req.local(false);
        req.humanReadable(true);
        req.includeDefaults(false);
        try {
            return restHighLevelClient.indices().exists(req, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("判断索引是否存在异常，detail:", e);
        }
        return false;
    }

    public boolean delIdx(String idx) {
        try {
            DeleteIndexRequest req = new DeleteIndexRequest(idx);
            AcknowledgedResponse resp = restHighLevelClient.indices().delete(req, RequestOptions.DEFAULT);
            return resp.isAcknowledged();
        } catch (Exception exception) {
            return false;
        }
    }

    public void saveOrUpdateDoc(String idx, ESDoc<?> doc) {
        IndexRequest req = new IndexRequest(idx);
        req.id(doc.getId());
        req.source(JsonUtils.toJson(doc.getData()), XContentType.JSON);
        try {
            if (!isExistIdx(idx)) {
                createIdx(idx, BeanUtils.objToEmbedMap(doc.getData()));
            }
            restHighLevelClient.index(req, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("ES 保存数据异常，detail:", e);
            throw new VServerException(e);
        }
    }

    public void saveOrUpdateDocs(String idx, List<ESDoc<?>> docs) {
        if (CollectionUtils.isEmpty(docs)) {
            return;
        }
        if (!isExistIdx(idx)) {
            createIdx(idx, BeanUtils.objToEmbedMap(docs.get(0).getData()));
        }
        BulkRequest req = new BulkRequest();
        docs.forEach(doc -> req.add(new IndexRequest(idx)
                .id(doc.getId())
                .source(JsonUtils.toJson(doc.getData()), XContentType.JSON)));
        try {
            restHighLevelClient.bulk(req, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("ES 批量保存数据异常，detail:", e);
            throw new VServerException(e);
        }
    }

    public void delDoc(String idx, String id) {
        DeleteRequest req = new DeleteRequest(idx, id);
        try {
            restHighLevelClient.delete(req, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("ES 删除文档数据异常，detail:", e);
            throw new VServerException(e);
        }
    }

    public void delDocByQuery(String idx, QueryBuilder queryBuilder) {
        DeleteByQueryRequest req = new DeleteByQueryRequest(idx).setQuery(queryBuilder);
        req.setConflicts("proceed");
        try {
            restHighLevelClient.deleteByQuery(req, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("ES 删除文档数据异常，detail:", e);
            throw new VServerException(e);
        }
    }

    public void delDocs(String idx, List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        BulkRequest req = new BulkRequest();
        ids.forEach(id -> req.add(new DeleteRequest(idx, id)));
        try {
            restHighLevelClient.bulk(req, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("ES 删除文档数据异常，detail:", e);
            throw new VServerException(e);
        }
    }

    public <T> T getDocById(String idx, String id, Class<T> clazz) {
        if (!isExistIdx(idx)) {
            return null;
        }
        GetRequest req = new GetRequest(idx, id);
        GetResponse resp;
        try {
            resp = restHighLevelClient.get(req, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("ES 查询文档数据异常，detail:", e);
            throw new VServerException(e);
        }
        String jsonStr = resp.getSourceAsString();
        return JsonUtils.fromJson(jsonStr, clazz);
    }

    public <T> List<T> searchByQuery(String idx, SearchSourceBuilder builder, Class<T> clazz) {
        if (!isExistIdx(idx)) {
            return null;
        }
        SearchRequest req = new SearchRequest(idx).source(builder);
        SearchResponse resp;
        try {
            resp = restHighLevelClient.search(req, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("ES 查询文档数据异常，detail:", e);
            throw new VServerException(e);
        }
        SearchHit[] hits = resp.getHits().getHits();
        List<T> results = new ArrayList<>(hits.length);
        for (SearchHit hit : hits) {
            String sourceAsString = hit.getSourceAsString();
            results.add(JsonUtils.fromJson(sourceAsString, clazz));
        }
        return results;
    }
}
