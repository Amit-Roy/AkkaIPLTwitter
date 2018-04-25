package com.twitter.akka.ipl;

import akka.actor.AbstractActor;
import akka.actor.Props;
import twitter4j.Logger;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TwitterActor extends AbstractActor {
    private Logger log = Logger.getLogger(getClass());
    private final Twitter twitter;

    public TwitterActor(Twitter twitter) {
        this.twitter = twitter;
    }

    public static Props props(Twitter twitter) {
        return Props.create(TwitterActor.class, () -> new TwitterActor(twitter));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TwitterMsg.class, this::tweet)
                .build();
    }

    private void tweet(TwitterMsg twitterMsg) {
        try {
            twitter.updateStatus(twitterMsg.message);
            log.info(twitterMsg.message);
        } catch (TwitterException te) {
            if (te.isErrorMessageAvailable()) {
                log.info("Not posted to twitter because: " + te.getMessage());
            }
        }
    }

    static public class TwitterMsg {
        public final String message;

        public TwitterMsg(String message) {
            this.message = message;
        }
    }
}
