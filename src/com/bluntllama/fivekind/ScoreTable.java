package com.bluntllama.fivekind;

import android.provider.BaseColumns;

public final class ScoreTable implements BaseColumns {
    // This class cannot be instantiated
    private ScoreTable() {
    }

    /**
     * The table name offered by this provider
     */
    public static final String TABLE_NAME = "scores";

    /**
     * The default sort order for this table
     */
    public static final String DEFAULT_SORT_ORDER = "tally_game DESC";

    /*
     * Column definitions
     */

    /**
     * Column name for the creation timestamp
     * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
     */
    public static final String COLUMN_NAME_GAME_DATE = "created";

    /**
     * Column name for the name of the player
     * <P>Type: INTEGER</P>
     */
    public static final String COLUMN_NAME_PLAYER_NAME = "player_name";

    /**
     * Column name for the score of ones
     * <P>Type: INTEGER</P>
     */
    public static final String COLUMN_NAME_SCORE_ONES = "score_ones";

    /**
     * Column name for the score of twos
     * <P>Type: INTEGER</P>
     */
    public static final String COLUMN_NAME_SCORE_TWOS = "score_twos";

    /**
     * Column name for the score of threes
     * <P>Type: INTEGER</P>
     */
    public static final String COLUMN_NAME_SCORE_THREES = "score_threes";

    /*
     * Column name for the score of fours
     * <P>Type: INTEGER</P>
     */
    public static final String COLUMN_NAME_SCORE_FOURS = "score_fours";

    /**
     * Column name for the score of fives
     * <P>Type: INTEGER</P>
     */
    public static final String COLUMN_NAME_SCORE_FIVES = "score_fives";

    /**
     * Column name for the score of sixes
     * <P>Type: INTEGER</P>
     */
    public static final String COLUMN_NAME_SCORE_SIXES = "score_sixes";

    /**
     * Column name for the basic section total
     * <P>Type: INTEGER</P>
     */
    public static final String COLUMN_NAME_TALLY_BASIC = "tally_basic";

    /**
     * Column name for the basic bonus
     * <P>Type: INTEGER</P>
     */
    public static final String COLUMN_NAME_TALLY_BASIC_BONUS = "tally_basic_bonus";

    /**
     * Column name for the basic total
     * <P>Type: INTEGER</P>
     */
    public static final String COLUMN_NAME_TALLY_BASIC_TOTAL = "tally_basic_tota1";

    /**
     * Column name for 2 pair same
     * <P>Type: INTEGER</P>
     */
    public static final String COLUMN_NAME_SCORE_TWO_PAIR = "score_two_pair";

    /**
     * Column name for the score of twos
     * <P>Type: INTEGER</P>
     */
    public static final String COLUMN_NAME_SCORE_3_KIND = "score_three_kind";

    /**
     * Column name for the score of the straight
     * <P>Type: INTEGER</P>
     */
    public static final String COLUMN_NAME_SCORE_STRAIGHT = "score_straight";

    /**
     * Column name for the score of the flush
     * <P>Type: INTEGER</P>
     */
    public static final String COLUMN_NAME_SCORE_FLUSH = "score_flush";

    /**
     * Column name for the score of full house
     * <P>Type: INTEGER</P>
     */
    public static final String COLUMN_NAME_SCORE_FULL_HOUSE = "score_full_house";

    /**
     * Column name for the score of full house same
     * <P>Type: INTEGER</P>
     */
    public static final String COLUMN_NAME_SCORE_FULL_HOUSE_SAME = "score_full_house_same";

    /**
     * Column name for the score of 4 of a kind
     * <P>Type: INTEGER</P>
     */
    public static final String COLUMN_NAME_SCORE_FOUR_KIND = "score_four_kind";

    /**
     * Column name for the score of yarborough
     * <P>Type: INTEGER</P>
     */
    public static final String COLUMN_NAME_SCORE_YARBOROUGH = "score_yarborough";

    /**
     * Column name for the score of kismet
     * <P>Type: INTEGER</P>
     */
    public static final String COLUMN_NAME_SCORE_FIVE_KIND = "score_five_kind";

    /**
     * Column name for the score of lower total
     * <P>Type: INTEGER</P>
     */
    public static final String COLUMN_NAME_TALLY_LOWER= "tally_lower";

    /**
     * Column name for the score of the game total
     * <P>Type: INTEGER</P>
     */
    public static final String COLUMN_NAME_TALLY_GAME = "tally_game";
}
