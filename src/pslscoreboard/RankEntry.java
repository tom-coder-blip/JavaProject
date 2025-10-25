/// The RankEntry class is a simple data holder that represents one row in the league ranking table.
/// This class doesn’t do any calculations itself; it’s mainly used to organize and display ranking results in the
/// scoreboard table.

package pslscoreboard;

/**
 * Simple holder used to render a ranked table row.
 */
public class RankEntry {
    private final int rank;
    private final Team team;

    public RankEntry(int rank, Team team) {
        this.rank = rank;
        this.team = team;
    }

    public int getRank() { return rank; }
    public Team getTeam() { return team; }
}
