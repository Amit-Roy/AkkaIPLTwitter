package com.twitter.akka.ipl;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.twitter.akka.ipl.TwitterActor.TwitterMsg;

import java.util.Set;

public class IPLActor extends AbstractActor {

    private CricketResource cricketResource;
    private Set<Integer> iplMatchIds;
    private ActorRef twitterActor;

    public IPLActor(ActorRef twitterActor, CricketResource cricketResource) {
        this.twitterActor = twitterActor;
        this.cricketResource = cricketResource;
    }

    static public Props props(ActorRef twitterActor, CricketResource cricketResource) {
        return Props.create(IPLActor.class, () -> new IPLActor(twitterActor, cricketResource));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(IPLMatchIds.class, x -> this.iplMatchIds = cricketResource.getIPLMatchIDs())
                .match(TweetScore.class, x -> tweet())
                .build();
    }

    private void tweet() {
        for (Integer id : iplMatchIds) {
            String score = cricketResource.getScore(id);
            if (score != null) {
                System.out.println("Twitter akka actor: " + score);
                twitterActor.tell(new TwitterMsg(score), getSelf());
            }
        }
    }

    public static class IPLMatchIds {

        public IPLMatchIds() {
        }

    }

    public static class TweetScore {

        public TweetScore() {
        }

    }
}