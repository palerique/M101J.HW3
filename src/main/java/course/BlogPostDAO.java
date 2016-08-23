package course;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class BlogPostDAO {

    public static final String PERMALINK = "permalink";
    public static final String TITLE = "title";
    public static final String AUTHOR = "author";
    public static final String BODY = "body";
    public static final String TAGS = "tags";
    public static final String COMMENTS = "comments";
    public static final String DATE = "date";
    public static final String EMAIL = "email";

    MongoCollection<Document> postsCollection;

    public BlogPostDAO(final MongoDatabase blogDatabase) {
        postsCollection = blogDatabase.getCollection("posts");
    }

    // Return a single post corresponding to a permalink
    public Document findByPermalink(String permalink) {

        Document post = postsCollection.find(Filters.eq(PERMALINK, permalink)).first();

        return post;
    }

    // Return a list of posts in descending order. Limit determines
    // how many posts are returned.
    public List<Document> findByDateDescending(int limit) {

        List<Document> posts = postsCollection.find()//
                .sort(Sorts.descending(DATE))//
                .limit(limit)//
                .into(new ArrayList<Document>());

        return posts;
    }


    public String addPost(String title, String body, List tags, String username) {

        System.out.println("inserting blog entry " + title + " " + body);

        String permalink = title.replaceAll("\\s", "_"); // whitespace becomes _
        permalink = permalink.replaceAll("\\W", ""); // get rid of non alphanumeric
        permalink = permalink.toLowerCase();

        Document post = new Document();
        post.append(TITLE, title)
                .append(AUTHOR, username)
                .append(BODY, body)
                .append(PERMALINK, permalink)
                .append(TAGS, tags)
                .append(COMMENTS, Collections.<Document>emptyList())
                .append(DATE, new Date());

        postsCollection.insertOne(post);

        return permalink;
    }


    // White space to protect the innocent


    // Append a comment to a blog post
    public void addPostComment(final String name, final String email, final String body,
                               final String permalink) {

        Document comment = new Document().append(AUTHOR, name).append(BODY, body);
        if (email != null) {
            comment.append(EMAIL, email);
        }

        Bson update = new Document("$push", new Document(COMMENTS, comment));
        postsCollection.updateOne(Filters.eq(PERMALINK, permalink), update, new UpdateOptions().upsert(true));
    }
}
