#!/usr/bin/env bash

set -ex
cat original.sql \
 | sed -e 's/INSERT INTO campaign_procedure (campaign_id/INSERT INTO campaign_procedure ("Campaign_id"/g' \
 | sed -e 's/INSERT INTO category_subject (category_id/INSERT INTO category_subject ("Category_id"/g' \
 | sed -e 's/INSERT INTO prioritylist_prioritylistitem (prioritylist_id/INSERT INTO prioritylist_prioritylistitem ("PriorityList_id"/g' \
 | sed -e 's/INSERT INTO "roleMapping_mandator" ("roleMapping_id"/INSERT INTO "roleMapping_mandator" ("RoleMapping_id"/g' \
 | sed -e 's/INSERT INTO ruleset_rule (ruleset_id/INSERT INTO ruleset_rule ("RuleSet_id"/g' \
 | sed -e 's/INSERT INTO timetable_occurence (timetable_id/INSERT INTO timetable_occurence ("Timetable_id"/g' \
 | cat <(printf "BEGIN;\n\n") - <(printf "COMMIT;\n") \
 | cat > migrate.sql
