/// The League class acts as the main manager of the entire football league.
/// It keeps track of all teams, processes match results, and calculates the overall rankings.

package pslscoreboard;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * League class manages all teams and processes match results.
 */
public class League {
    // Stores all teams in a map (key = team name in lowercase, value = Team object)
    private final Map<String, Team> teams = new HashMap<>();

    // Regex pattern to read lines like "Team A 2, Team B 1"
    private static final Pattern LINE_PATTERN = Pattern.compile("^\\s*(.+?)\\s+(\\d+)\\s*,\\s*(.+?)\\s+(\\d+)\\s*$");

    // Constructor
    public League() {}

    // Makes sure a team exists; if not, creates it
    public void ensureTeam(String name) {
        String key = normalize(name);
        teams.computeIfAbsent(key, k -> new Team(name.trim()));
    }

    // Clears all teams from the league
    public void clear() {
        teams.clear();
    }

    /**
     * Reads one match line like "Pirates 1, Chiefs 2"
     * Updates both teams' stats and returns true if successful.
     */
    public boolean processLine(String line) {
        if (line == null) return false;
        Matcher m = LINE_PATTERN.matcher(line);
        if (!m.matches()) return false; // if line doesn’t match pattern, skip

        // Extract team names and scores from the line
        String teamAName = m.group(1).trim();
        int goalsA = Integer.parseInt(m.group(2));
        String teamBName = m.group(3).trim();
        int goalsB = Integer.parseInt(m.group(4));

        // Make sure both teams exist
        ensureTeam(teamAName);
        ensureTeam(teamBName);

        // Get both teams from the map
        Team a = teams.get(normalize(teamAName));
        Team b = teams.get(normalize(teamBName));

        // Update stats for both teams
        a.addMatch(goalsA, goalsB);
        b.addMatch(goalsB, goalsA);

        return true;
    }

    // Converts a name to lowercase for consistent lookups
    private String normalize(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }

    // Adds a list of teams to the league
    public void seedTeams(Collection<String> names) {
        for (String n : names) ensureTeam(n);
    }

    /**
     * Returns a ranked list of teams based on points.
     * - Higher points rank higher
     * - Same points → alphabetical order
     * - Teams with same points share the same rank
     */
    public List<RankEntry> getRanking() {
        // Convert team map to a list
        List<Team> list = new ArrayList<>(teams.values());

        // Sort teams by points (desc), then name (asc)
        list.sort((t1, t2) -> {
            if (t2.getPoints() != t1.getPoints())
                return Integer.compare(t2.getPoints(), t1.getPoints());
            return t1.getName().compareToIgnoreCase(t2.getName());
        });

        List<RankEntry> ranked = new ArrayList<>();
        int currentRank = 1;
        int position = 0;

        // Loop through all teams to assign ranks
        while (position < list.size()) {
            Team t = list.get(position);
            int points = t.getPoints();

            // Count how many teams have the same points
            int count = 0;
            for (int i = position; i < list.size(); i++) {
                if (list.get(i).getPoints() == points) count++;
                else break;
            }

            // Add each team with the same points using the same rank number
            for (int i = 0; i < count; i++) {
                ranked.add(new RankEntry(currentRank, list.get(position + i)));
            }

            // Next rank starts after all tied teams
            position += count;
            currentRank += count;
        }

        return ranked;
    }

    // Returns all teams (useful for displaying or debugging)
    public Collection<Team> getTeams() {
        return new ArrayList<>(teams.values());
    }
}