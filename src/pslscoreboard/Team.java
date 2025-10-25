/// The Team class represents a single football team in the PSL Scoreboard system.
/// this class models one team’s performance in the league and provides all the data needed to calculate rankings.

package pslscoreboard; // Package name (organizes related classes)

public class Team {
    private final String name; // Team name (cannot be changed once set)
    private int points = 0; // Total points earned
    private int goalsFor = 0; // Total goals scored
    private int goalsAgainst = 0; // Total goals conceded
    private int matchesPlayed = 0; // Number of matches played

    // Constructor: creates a new team with a given name
    public Team(String name) {
        this.name = name.trim(); // Removes spaces around the name
    }

    // Getter methods (to read private values)
    public String getName() { return name; }
    public int getPoints() { return points; }
    public int getGoalsFor() { return goalsFor; }
    public int getGoalsAgainst() { return goalsAgainst; }
    public int getMatchesPlayed() { return matchesPlayed; }

    // Calculates and returns goal difference (goalsFor - goalsAgainst)
    public int getGoalDifference() { return goalsFor - goalsAgainst; }

    // Updates stats after a match
    public void addMatch(int goalsForThisMatch, int goalsAgainstThisMatch) {
        this.matchesPlayed++; // Increase matches played by 1
        this.goalsFor += goalsForThisMatch; // Add goals scored
        this.goalsAgainst += goalsAgainstThisMatch; // Add goals conceded

        // Add points: 3 for win, 1 for draw, 0 for loss
        if (goalsForThisMatch > goalsAgainstThisMatch) {
            this.points += 3;
        } else if (goalsForThisMatch == goalsAgainstThisMatch) {
            this.points += 1;
        }
    }

    // Returns a readable summary of the team's stats
    @Override
    public String toString() {
        return String.format("%s — %d pts (GF:%d GA:%d GD:%d)",
                name, points, goalsFor, goalsAgainst, getGoalDifference());
    }
}