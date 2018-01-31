import json
import numpy as np
import matplotlib.pyplot as plt
import os
from scipy.stats.stats import pearsonr 
from scipy.stats.stats import spearmanr
from sklearn.model_selection import train_test_split
from sklearn import ensemble
from sklearn.dummy import DummyClassifier
from sklearn.linear_model import LinearRegression
from sklearn.metrics import confusion_matrix
from sklearn.metrics import accuracy_score
from sklearn.metrics import cohen_kappa_score
from prettytable import PrettyTable

def getBuildResults(database):
    with open(database) as json_data:
        t = json.load(json_data)

    statuses = t['statuses']

    clearedStats = []
    firstIt = True
    for k, v in sorted(statuses.items(), key=lambda statuses: int(statuses[0])):
        if firstIt:
            firstIt = False
            continue
        clearedStats.append(int(v=='passed'))
    
    return clearedStats
    

def getCompilableStats(databaseFile, compFile):
    with open(databaseFile) as json_data:
        d = json.load(json_data)

    commits = d['commits']
    
    with open(compFile) as json_data:
        c = json.load(json_data)
       
    compList = [] 
    firstIt = True
    for k, v in sorted(commits.items(), key=lambda commits: int(commits[0])):
        if firstIt:
            firstIt = False
            continue
        y = next((x for x in c if x['commitID'] == v), None)
        compList.append(int(y['compilable']))
    return compList
        
    
def getVersionDiff(versionDiffFile):
    with open(versionDiffFile) as json_data:
        d = json.load(json_data)
             
    a2a = []
    relInst = []
    absInst = []
    cvgFrom = []
    cvgTarget = []
    numNodes = []
    numEdges = []
    deg = []
    for k in sorted(d, key=lambda d: d['toVersion']):
        
        relInst.append(float(k['metrics']['global']['avgRelInst']))
        absInst.append(float(k['metrics']['global']['avgAbsInst']))
        a2a.append(float(k['metrics']['arcade']['a2a']))
        cvgFrom.append(float(k['metrics']['arcade']['cvgSource']))
        cvgTarget.append(float(k['metrics']['arcade']['cvgTarget']))
        numNodes.append(float(k['metrics']['global']['numNodes']))
        numEdges.append(float(k['metrics']['global']['numEdges']))
        deg.append(float(k['metrics']['global']['avgNodeDeg']))

    a = np.zeros((len(relInst), 8))
    for i in range(len(relInst)):
        a[i][0] = numNodes[i]
        a[i][1] = numEdges[i]
        a[i][2] = absInst[i]
        a[i][3] = relInst[i]
        a[i][4] = deg[i]
        a[i][5] = a2a[i]
        a[i][6] = cvgFrom[i]
        a[i][7] = cvgTarget[i]
        
    return a
    
def booleanize(y):
    # Booleanize:
    z = np.zeros(y.shape)
    z[y != 0] = 1
    return z
    
def getPrevNext(y, threshold):
    z = booleanize(y)
    
    # Biased because of Cross Projects
    afterStats = []
    for k in range(len(z)):
        sum = 0
        for i in range(threshold):
            if (k+i) < len(z)-1:
                sum += y[k+i]
        afterStats.append(sum)

    beforeStats = []
    for k in range(len(z)):
        sum = 0
        for i in range(threshold):
            if (k-i) > 0:
                sum += z[k-i]
        beforeStats.append(sum)
        
    return (beforeStats, afterStats)
    
def getStatistics(A, y):

    prNx_threshold = [2, 3, 5, 10]
    change_threshold = [0, 0.01, 0.02, 0.03, 0.04, 0.05, 0.1, 0.2, 0.5]

    for feature in range(A.shape[1]):
        print('\n')
        print('#'*150)
        print(featureList[feature])
        
        samples = A[:, feature]
        
        print('M vs Out ' + str(pearsonr(samples, y)))
        
        for ch_th in change_threshold:
            B = (A[:,feature]>ch_th).astype(int)
            print('Changes over Threshold ' + str(ch_th) + ': ' + str((B == 1).sum())) 
            if ((B==1).sum()) > 0:
                print('Ch (' + str(ch_th) + ') vs Out : ' + str(spearmanr(B, y)))
                failsIfChange = 0
                for i in range(len(B)):
                    if B[i] == 1 and y[i] != 0:
                        failsIfChange += 1
                print('P(fail | change): ' + str(failsIfChange) + '/' + str((B==1).sum()) + ' = ' + str(failsIfChange / (B==1).sum()))
                print('P(change | fail): ' + str(failsIfChange) + '/' + str((y!=0).sum()) + ' = ' + str(failsIfChange / (y!=0).sum()))
                        
        for pr_th in prNx_threshold:
            (before, after) = getPrevNext(y, pr_th)
            print('M vs Bef (' + str(pr_th) + '): ' + str(pearsonr(samples[pr_th:], before[pr_th:])))
            print('M vs Nxt (' + str(pr_th) + '): ' + str(pearsonr(samples[pr_th:], after[pr_th:])))
            
            for ch_th in change_threshold:
                B = (A[:,feature]>ch_th).astype(int)
                if ((B==1).sum()) > 0:
                    print('Ch (' + str(ch_th) + ') vs Bef (' + str(pr_th) + '): ' + str(spearmanr(B[pr_th:], before[pr_th:])))
                    print('Ch (' + str(ch_th) + ') vs Nxt (' + str(pr_th) + '): ' + str(spearmanr(B[pr_th:], after[pr_th:])))
        
        print('#'*150)
        
def plotSpecific(A, y):
    change_threshold = np.arange(100) * 0.01
    samples = A[:, 5]
    (before, after) = getPrevNext(y, 10)
    (beforeE, afterE) = getPrevNext(y, 80)
    corr = []
    corrE = []
    for ch_th in change_threshold:
        B = (samples>ch_th).astype(int)
        (co, p) = spearmanr(B[10:], after[10:])
        corr.append(co)
        (coE, pE) = spearmanr(B[80:], afterE[80:])
        corrE.append(coE)
    l1, = plt.plot(change_threshold, corr, 'b')
    l2, = plt.plot(change_threshold, corrE, 'r')
    plt.legend([l1, l2], ['Next 10 Builds', 'Next 80 Builds'], loc=1)
    plt.xlim([0, 1])
    plt.ylabel('Correlation')
    plt.xlabel('Change Threshold')
    plt.title('Spearman: ' + featureList[5] + ' vs Builds')
    plt.show()
    
    next_threshold = np.arange(1, 250)
    B = (samples>0.0).astype(int)
    corr = []
    for nx_th in next_threshold:
        (before, after) = getPrevNext(y, nx_th)
        (co, p) = spearmanr(B[nx_th:], before[nx_th:])
        corr.append(co)
    plt.plot(next_threshold, corr)
    plt.xlim([1, 250])
    plt.ylabel('Correlation')
    plt.xlabel('Previous Builds')
    plt.title('Spearman: ' + featureList[5] + ' at (0.0) vs Previous n Builds')
    plt.show()
        
    
def machineLearn(A, y):

    X_train, X_test, y_train, y_test = train_test_split(A, y, test_size=0.33, stratify=y)

    clf = ensemble.RandomForestClassifier()
    clf.fit(X_train, y_train)
    pred = clf.predict(X_test)

    tn, fp, fn, tp = confusion_matrix(y_test, pred).ravel()
    print ( accuracy_score(y_test, pred))
    print (cohen_kappa_score(y_test, pred))

    print('TN: ' + str(tn))
    print('TP: ' + str(tp))
    print('FP: ' + str(fp))
    print('FN: ' + str(fn))
    
def plot(A):
    binwidth=0.01
    #maximum = [350, 400, 300, 200, 300, 700, 1500 , 1500]
    for feature in range(A.shape[1]):
        values = A[:, feature]
        values = list(filter(lambda a: a != 0, values))
        #print(featureList[feature])
        #print('Min: ' + str(min(A[:, feature])))
        #print('Max: ' + str(max(A[:, feature])))
        plt.hist(values, bins=np.arange(min(values), max(values) + binwidth, binwidth))
        plt.xlim([0,1])
        #plt.ylim([0, maximum[feature]])
        plt.xlabel('Change percentage')
        plt.ylabel('# Builds')
        plt.title(featureList[feature])
        #plt.show()
        plt.savefig(featureList[feature] + '.pdf')
        plt.close()

def metricCorr(A):
    t = PrettyTable()
    t.field_names = [''] + featureList

    for i in range(8):
        row = [featureList[i]] + ['', '', '', '', '', '', '', ''] 
        for j in range(8):
        
            (v, p) = pearsonr(A[:,i], A[:,j])
            row[j+1] = format(v, '.2g') + ', ' + format(p, '.2g')
            
        t.add_row(row)
    print(t)
    
def getOutlier(A):
    degree = A[:, 7]
    print(degree.shape)
    
    print(-np.partition(-degree, 3)[:3])
    
def checkCompVsBuildRes(results, comp):
    results = 1-results

    booleanComp = np.copy(comp)
    booleanComp[comp > 1] = 1
    
    res = []
    for i, val in enumerate(results):
        if val == 0:
            res.append(booleanComp[i] == 0)
        else: 
            res.append(True)
    return np.array(res)

def countCompileAndDependencyErrors(comp):
    return (comp == 2).sum() + (comp == 1).sum()
    
def countErrors(comp):
    return np.count_nonzero(comp != 0)
    
def is_non_zero_file(fpath):  
    # Five bytes to counter empty json arrays
    return os.path.isfile(fpath) and os.path.getsize(fpath) > 5
    
def mergeLogsAndBuildRes(logs, buildRes):
    merged = np.zeros(y.shape)
    for ind, val in enumerate(y):
        if val == 1:
            merged[ind] = 0
        else:
            if c[ind] != 0:
                merged[ind] = c[ind]
            else:
                merged[ind] = 4
    return merged

featureList = ['NumNodes', 'NumEdges', 'AbsInst', 'RelInst', 
    'NodeDegree', 'a2a', 'cvgSource', 'cvgTarget']

#0 = NO comp error
#1 = build passed

baseFol = 'combined/'

buildResults = []
comp = []
for project in os.listdir(baseFol):
    databaseFile = baseFol + project + '/database.json'
    compilableFile = baseFol + project + '/compilable.json'
    buildResults.append(getBuildResults(databaseFile))
    print(len(getBuildResults(databaseFile)))
    comp.append(getCompilableStats(databaseFile, compilableFile))
buildResults = np.array([z for x in buildResults for z in x])
comp = np.array([z for x in comp for z in x])

print('All analyzed builds: ' + str(len(comp)))
print('Num of errors: ' + str(countErrors(comp)))
print('Num of compilation or dependency errors: ' + str(countCompileAndDependencyErrors(comp)))
print('No Error: ' + str((comp == 0).sum()))
print('Dependency Error: ' + str((comp == 1).sum()))
print('Compilation Error: ' + str((comp == 2).sum()))
print('Test Error: ' + str((comp == 3).sum()))
print('Other Error: ' + str((comp == 4).sum()))
#print((checkCompVsBuildRes(buildResults, comp) == False).sum())
print('Builds Failed: ' + str((buildResults == 0).sum()))

print('\n')
print('Successful analyzed projects')
print('\n')

A = []
y = []
c = []
numProjects = 0

for project in os.listdir(baseFol):
    versionDiffFile = baseFol + project + '/versionDiff.json'
    databaseFile = baseFol + project + '/database.json'
    compilableFile = baseFol + project + '/compilable.json'
    
    if is_non_zero_file(versionDiffFile):        
        numProjects += 1
        A.append(getVersionDiff(versionDiffFile))
        y.append(getBuildResults(databaseFile))
        c.append(getCompilableStats(databaseFile, compilableFile))
A = np.array([z for x in A for z in x])
y = np.array([z for x in y for z in x])
c = np.array([z for x in c for z in x])

#y: 1 = build passed
#c, merged
#public static int NO_ERROR = 0;
#public static int DEPENDENCY_ERROR = 1;
#public static int COMPILATION_ERROR = 2;
#public static int TEST_ERROR = 3;
#public static int UNKNOWN_ERROR = 4;

merged = mergeLogsAndBuildRes(c, y)
            
print('Number of Projects: ' + str(numProjects))
#c[c == 2] = 1
#c[c == 3] = 0
#print(c)

print('All analyzed builds: ' + str(len(merged)))
print('Num of errors: ' + str(countErrors(merged)))
print('Num of compilation or dependency errors: ' + str(countCompileAndDependencyErrors(merged)))
print('No Error: ' + str((merged == 0).sum()))
print('Dependency Error: ' + str((merged == 1).sum()))
print('Compilation Error: ' + str((merged == 2).sum()))
print('Test Error: ' + str((merged == 3).sum()))
print('Other Error: ' + str((merged == 4).sum()))

passed = 0
for i in range(len(merged)):
    if merged[i] == 0:
        passed += 1
        
print('Pass Rate: ' + str(passed) + ' / ' + str(len(c)))
print('Passes: ' + str(passed / len(c)))

print('Change Rate per Metric: ' + str(np.count_nonzero(A, axis=0) / passed))

metricCorr(A)
getStatistics(A, merged)
machineLearn(A, booleanize(merged))
plot(A)
plotSpecific(A, merged)

