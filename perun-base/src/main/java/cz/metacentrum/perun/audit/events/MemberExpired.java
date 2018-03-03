package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.Member;

public class MemberExpired {
    private Member member;


    public MemberExpired(Member member) {
        this.member = member;
    }

    @Override
    public String toString() {
        return member + " expired.";
    }
}
