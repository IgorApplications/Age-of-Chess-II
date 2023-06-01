package com.iapp.ageofchess.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.multiplayer.MultiplayerEngine;
import com.iapp.ageofchess.services.ChessConstants;
import com.iapp.lib.ui.widgets.AccountView;
import com.iapp.lib.web.Account;
import com.iapp.lib.web.RankType;
import com.iapp.lib.ui.actors.RdLabel;
import com.iapp.lib.ui.actors.RdTable;
import com.iapp.lib.util.OnChangeListener;

public class RankView extends RdTable implements Disposable {

    private static final Color GREEN = new Color(Color.rgba8888(0f, 1f, 0.12f, 1));
    private static final Color BLUE = new Color(Color.rgba8888(0f, 0.58f, 1f, 1));
    private static final Color YELLOW = new Color(Color.rgba8888(1f, 0.847f, 0f, 1));
    private static final Color ORANGE = Color.ORANGE;
    private static final Color RED = Color.RED;
    private static final Color VIOLET = new Color(Color.rgba8888(0.34f, 0f, 1f, 1));

    private final AccountView accountView;
    private final double rank;
    private final int number;

    public RankView(int number, Account account, RankType rankType) {
        accountView = new AccountView(MultiplayerEngine.self(), account,
            new OnChangeListener() {
                @Override
                public void onChange(Actor actor) {
                    ChessConstants.accountController.seeAccount(account.getId());
                }
        });
        rank = getRank(account, rankType);
        this.number = number;
        init();
    }

    @SuppressWarnings("DefaultLocale")
    private void init() {
        setBackground(new NinePatchDrawable(
            new NinePatch(ChessApplication.self().getAssetManager().findRegion("lite_pane"),
                10,10,10,10)));
        RdLabel rankText = new RdLabel(String.format("%.2f", rank));
        rankText.setColor(getRankColor(rank));

        add(new RdLabel(number + ". ")).minWidth(50);
        add(accountView);
        add(rankText).expandX().align(Align.right).padRight(20);
    }

    private double getRank(Account account, RankType rankType) {
        switch (rankType) {
            case BULLET: return account.getBullet();
            case BLITZ: return account.getBlitz();
            case RAPID: return account.getRapid();
            case LONG: return account.getLongRank();
        }
        return 0;
    }

    private Color getRankColor(double rank) {
        if (rank >= 3000) {
            return VIOLET;
        } else if (rank >= 2500) {
            return RED;
        } else if (rank >= 2300) {
            return ORANGE;
        } else if (rank >= 2000) {
            return YELLOW;
        } else if (rank >= 1500) {
            return BLUE;
        }
        return GREEN;
    }

    @Override
    public void dispose() {
        accountView.dispose();
    }
}
