import json
import numpy as np
import matplotlib.pyplot as plt
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

def getDatabase(database):
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
        
    for k, v in sorted(commits.items(), key=lambda commits: int(commits[0])):
        print(k)
        print(v)
    
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

def getData(database, versionDiff):
    return (getVersionDiff(versionDiff), getDatabase(database))
    
def getPrevNext(y, threshold):
    # Biased because of Cross Projects
    afterStats = []
    for k in range(len(y)):
        sum = 0
        for i in range(threshold):
            if (k+i) < len(y)-1:
                sum += y[k+i]
        afterStats.append(sum)

    beforeStats = []
    for k in range(len(y)):
        sum = 0
        for i in range(threshold):
            if (k-i) > 0:
                sum += y[k-i]
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
        
        print('M vs Out ' + str(spearmanr(samples, y)))
        
        for ch_th in change_threshold:
            B = (A[:,feature]>ch_th).astype(int)
            print('Changes over Threshold ' + str(ch_th) + ': ' + str((B == 1).sum())) 
            if ((B==1).sum()) > 0:
                print('Ch (' + str(ch_th) + ') vs Out : ' + str(spearmanr(B, y)))
        
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
    samples = A[:, 6]
    (before, after) = getPrevNext(y, 5)
    corr = []
    for ch_th in change_threshold:
        B = (samples>ch_th).astype(int)
        (co, p) = spearmanr(B[5:], after[5:])
        corr.append(co)
    plt.plot(change_threshold, corr)
    plt.xlim([0, 1])
    plt.ylabel('Correlation')
    plt.xlabel('Change Threshold')
    plt.title('Spearman: ' + featureList[6] + ' vs Next 5 Builds')
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
    

featureList = ['NumNodes', 'NumEdges', 'AbsInst', 'RelInst', 
    'NodeDegree', 'a2a', 'cvgSource', 'cvgTarget']

A1, y1 = getData('database/sonarqube.json', 'diffs/versionDiff-sonarqube.json')
A2, y2 = getData('database/graylog2-server.json', 'diffs/versionDiff-graylog.json')
A3, y3 = getData('database/okhttp.json', 'diffs/versionDiff-okhttp.json')
A4, y4 = getData('database/cloudify.json', 'diffs/versionDiff-cloudify.json')
A5, y5 = getData('database/structr.json', 'diffs/versionDiff-structr.json')
A6, y6 = getData('database/owlapi.json', 'diffs/versionDiff-owlapi.json')
A7, y7 = getData('database/jOOQ.json', 'diffs/versionDiff-jooq.json')
A8, y8 = getData('database/checkstyle.json', 'diffs/versionDiff-checkstyle.json')
A9, y9 = getData('database/vectorz.json', 'diffs/versionDiff-vectorz.json')
A10, y10 = getData('database/java-driver.json', 'diffs/versionDiff-javaDriver.json')
    
A = np.concatenate((A1, A2, A3, A4, A5, A6, A7, A8, A9, A10), axis=0)
y = y1 + y2 + y3 + y4 + y5 + y6 + y7 + y8 + y9 + y10


passed = 0
for i in range(len(y)):
    if y[i] == True:
        passed += 1
        
print(str(passed) + ' / ' + str(len(y)))
print('Passes: ' + str(passed / len(y)))

print(np.count_nonzero(A, axis=0) / passed)

#metricCorr(A)
#getStatistics(A, y)
#machineLearn(A, y)
#plot(A)
#plotSpecific(A, y)
