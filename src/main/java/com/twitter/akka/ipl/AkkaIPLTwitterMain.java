package com.twitter.akka.ipl;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.twitter.akka.ipl.IPLActor.IPLMatchIds;
import com.twitter.akka.ipl.IPLActor.TweetScore;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

import java.io.IOException;
import java.util.Properties;

public class AkkaIPLTwitterMain {

    private ActorRef iplActor;
    private ActorSystem system;

    public static void main(String[] args) throws IOException, InterruptedException {
        AkkaIPLTwitterMain main = new AkkaIPLTwitterMain();
        main.run();
    }

    private AkkaIPLTwitterMain() throws IOException {
        // Load auth props
        Properties properties = getAuthProperties();

        // Prepare Twitter Object
        Twitter twitter = getConfiguredTwitter(properties);

        // Init Cricket Resource
        CricketResource cricketResource = new CricketResource(properties);

        // Prepare actor system
        system = ActorSystem.create("twitterAkka");
        ActorRef twitterActor = system.actorOf(TwitterActor.props(twitter));
        iplActor = system.actorOf(IPLActor.props(twitterActor, cricketResource));

    }

    private Twitter getConfiguredTwitter(Properties properties) {
        Twitter twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(properties.getProperty("consumer.key"), properties.getProperty("consumer.secret"));
        AccessToken accessToken = new AccessToken(properties.getProperty("access.token"), properties.getProperty("access.token.secret"));
        twitter.setOAuthAccessToken(accessToken);
        return twitter;
    }

    private Properties getAuthProperties() throws IOException {
        Properties properties = new Properties();
        properties.load(TwitterActor.class.getClassLoader().getResourceAsStream("auth.properties"));
        return properties;
    }

    private void run() throws InterruptedException {
        try {

            while (true) {
                iplActor.tell(new IPLMatchIds(), ActorRef.noSender());
                iplActor.tell(new TweetScore(), ActorRef.noSender());
                Thread.sleep(10000L);
            }

        } finally {
            system.terminate();
        }
    }
}
