function(doc){
  var tupe = doc['@type'];
  if ( tupe != "foo" ) {
    return;
  }

  emit( [doc.aValue , doc.description], doc.description );
}