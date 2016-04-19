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


wordTokenizer = RegexpTokenizer("[\w']+")


finalOutputFile = open("preprocessed_"+sys.argv[1], 'w')
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
    tokens = wordTokenizer.tokenize(reviewSTR.lower())
    finalOutputFile.write(sys.argv[2]+str(dummy_name)+" " + sys.argv[2] + " " + " ".join(tokens) + "\n") #name label data
    dummy_name+=1

