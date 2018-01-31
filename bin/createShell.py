import pymysql
import copy 

conn = pymysql.connect(host='localhost', user='archi', passwd='archi', db='archi')
cur = conn.cursor()
cur.execute('select gh_project_name from travistorrent_8_2_2017 where gh_lang = "java" and tr_log_analyzer="java-maven" and git_branch = "master" group by gh_project_name order by count(gh_project_name) desc;')
commitList = copy.copy(cur)

f = open('createDatabase.sh', 'w')
g = open('runProjects.sh', 'w')
f.write('#!/bin/bash\n')
g.write('#!/bin/bash\n')
for commit in commitList:
    if True:
        #id = commit[0]
        body = commit[0]

        f.write('java -jar architecture-0.0.1-SNAPSHOT-shaded.jar ' + body + ' -1')
        g.write('java -jar architecture-0.0.1-SNAPSHOT-shaded.jar ' + body + ' 0')
        f.write('\n')
        g.write('\n')
f.close()
g.close()
