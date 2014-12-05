#!/usr/bin/env bash
#
# Runs the English PCFG parser on one or more files, printing dependency parses only

if [ ! $# -ge 1 ]; then
  echo Usage: `basename $0` 'file(s)'
  echo
  exit
fi

scriptdir=`dirname $0`

java -mx10g -cp "$scriptdir/*:" edu.stanford.nlp.parser.lexparser.LexicalizedParser -nthreads 2 -sentences newline \
 -retainTmpSubcategories -outputFormat "typedDependencies" -outputFormatOptions "basicDependencies" edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz $*
