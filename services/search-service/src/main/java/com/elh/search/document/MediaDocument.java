package com.elh.search.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "medias")
public class MediaDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String guildId;

    @Field(type = FieldType.Keyword)
    private String channelId;

    @Field(type = FieldType.Keyword)
    private String authorId;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String authorName;

    @Field(type = FieldType.Keyword)
    private String mediaType;

    @Field(type = FieldType.Keyword)
    private String mimeType;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String permanentUrl;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String thumbnailUrl;

    @Field(type = FieldType.Text, analyzer = "standard")
    private List<String> tags;

    @Field(type = FieldType.Long)
    private Long sizeBytes;

    @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
    private Instant createdAt;
}
