#!/bin/sh
NAME=${1-movies}
TARGETPATH=${2-/tmp}
TARGET="$TARGETPATH/$NAME"
if [ ! -d $TARGET ]; then
    git clone https://github.com/neo4j-graph-examples/$NAME $TARGET
fi

QUERY=`pcregrep -M '^:query:|.*\+\s*$' $TARGET/README.adoc | cut -d' ' -f2- | sed -e 's/\+$//g'` 
EXPECT=`grep :expected-result: $TARGET/README.adoc | cut -d' ' -f2-`
PARAMNAME=`grep :param-name: $TARGET/README.adoc | cut -d' ' -f2-`
PARAMVALUE=`grep :param-value: $TARGET/README.adoc | cut -d' ' -f2-`
RESULTCOLUMN=`grep :result-column: $TARGET/README.adoc | cut -d' ' -f2-`

mkdir -p $TARGET/code
pushd code

    for file in `ls */?xample.*`; do
        LANG=${file%%/*}
        echo "Updating $file"
        mkdir -p $TARGET/code/$LANG
        cp $file $TARGET/code/$file
        indent=`grep 'MATCH (m:Movie' $TARGET/code/$file | cut -d'M' -f1 | cut -d'"' -f1`
        if [ $LANG == "java" ]; then
          Q2=`/bin/echo -n "$QUERY" | sed -e "s/\(.*\)/$indent\"\1\" +/g" | tr '\n' '§' | sed -e 's/\+§$/;§/g'`
        else
          Q2=`/bin/echo -n "$QUERY" | sed -e "s/\(.*\)/$indent\1/g" | tr '\n' '§'`
        fi
        sed -i.tmp -e "s/^.*MATCH (m:Movie.*$/$Q2/g" $TARGET/code/$file
        mv $TARGET/code/$file $TARGET/code/$file.tmp
        tr '§' '\n' < $TARGET/code/$file.tmp > $TARGET/code/$file

        URL="neo4j+s:\/\/demo.neo4jlabs.com:7687"
        BOLTURL="bolt:\/\/<HOST>:<BOLTPORT>"
#        sed -i.tmp -e "s/$URL/$BOLTURL/g" $TARGET/code/$file
        sed -i.tmp -e "s/movieTitle/$PARAMNAME/g" $TARGET/code/$file
        sed -i.tmp -e "s/The Matrix/$PARAMVALUE/g" $TARGET/code/$file
        sed -i.tmp -e "s/actorName/$RESULTCOLUMN/g" $TARGET/code/$file
        # database
        sed -i.tmp -e "s/movies/neo4j/g" $TARGET/code/$file
        sed -i.tmp -e "s/mUser/<USERNAME>/g" $TARGET/code/$file
        sed -i.tmp -e "s/s3cr3t/<PASSWORD>/g" $TARGET/code/$file

        rm $TARGET/code/$file.tmp
    done
popd