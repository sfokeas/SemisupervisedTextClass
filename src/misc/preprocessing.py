import sys
import string
import json
import nltk.data
from nltk.tokenize.regexp import RegexpTokenizer
from nltk import word_tokenize, sent_tokenize
import subprocess
import string
import ast
import pickle


#usage app inputFile category
#output in the same dir wih name like inputFIle + proprocessed


#wordTokenizer = RegexpTokenizer("[\w']+")


finalOutputFile = open(sys.argv[1]+"_preprocessed", 'w')
reviewsJSONFile = open(sys.argv[1], "r")

linenumber =0
dummy_name = 0

for line in reviewsJSONFile:
    if linenumber%1000 ==0:
        print(linenumber)
    linenumber+=1
    objJSON = json.loads(line)
    #tokenize and clean the review text
    reviewSTR = objJSON['reviewText']
    reviewSTR = ''.join(ch.lower() for ch in reviewSTR if ch not in set(string.digits)) #remove digits and transform to lower case
    #tokens = wordTokenizer.tokenize(reviewSTR.lower())
    tokens = word_tokenize(reviewSTR)
    finalOutputFile.write(sys.argv[2]+
                          str(dummy_name)+" "
                          + sys.argv[2] + " "
                          + ' '.join(token for token in tokens if token not in set(string.punctuation + string.digits))
                          + "\n") #name label data
    dummy_name+=1

