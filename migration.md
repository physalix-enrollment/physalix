# Migrate data

```shell
sudo su postgres
pg_dump --file="dump.sql" --data-only --column-inserts awp 
```

Find duplicate rz-identities.

in: test-dump-columns-user-cleaned.sql
out: test-dump-columns.sql
./transform.sh

Execute sql script in database
