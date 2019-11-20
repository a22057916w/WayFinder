import multiprocessing as mp
from dumpDist import MYSQL_DUMP_DIST
from dumpJson import MYSQL_DUMP_JSON
from dumpNext import MYSQL_DUMP_NEXT
from dumpVertex import MYSQL_DUMP_VERTEX
from dumpPoster import MYSQL_DUMP_POSTER

if __name__ == '__main__':
    func = [MYSQL_DUMP_DIST, MYSQL_DUMP_JSON, MYSQL_DUMP_NEXT, MYSQL_DUMP_VERTEX, MYSQL_DUMP_POSTER]
    n = len(func)
    p = [None] * n
    for i in range(0, n):
        p[i] = mp.Process(target=func[i])
        p[i].start()
    for i in range(0, n):
        p[i].join()
    for i in range(0, n):
        p[i].close()
